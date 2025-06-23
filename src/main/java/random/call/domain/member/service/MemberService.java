package random.call.domain.member.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import random.call.domain.call.CallParticipant;
import random.call.domain.friendList.repository.FriendRepository;
import random.call.domain.friendList.type.FriendStatus;
import random.call.domain.friendRequest.FriendRequest;
import random.call.domain.friendRequest.FriendRequestRepository;
import random.call.domain.member.DeviceInfo;
import random.call.domain.member.Member;
import random.call.domain.member.RequiredConsent;
import random.call.domain.member.repository.MemberRepository;
import random.call.domain.member.QuestionAnswer;
import random.call.domain.member.dto.*;
import random.call.domain.member.repository.QuestionAnswerRepository;
import random.call.domain.member.repository.RequiredConsentRepository;
import random.call.domain.member.type.MBTI;
import random.call.global.exception.enums.ErrorCode;
import random.call.global.exception.exceptions.AuthException;
import random.call.global.exception.exceptions.MemberException;
import random.call.global.jwt.JwtUtil;
import random.call.global.security.userDetails.CustomUserDetails;

import java.util.List;
import java.util.Random;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final MemberRepository memberRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final QuestionAnswerRepository questionAnswerRepository;
    private final RequiredConsentRepository requiredConsentRepository;

    private final PasswordEncoder passwordEncoder;
    private final FriendRepository friendRepository;
    private final JwtUtil jwtUtil;

    //멤버조회
    @Transactional(readOnly = true)
    public MemberResponseDTO getMember(Long memberId) {

        Member member = findMemberById(memberId);
        return new MemberResponseDTO(member);
    }



    @Transactional(readOnly = true)
    public MemberProfileResponseDTO getProfileById(Long requestMemberId, Long targetMemberId) {
        Member member = memberRepository.findById(targetMemberId)
                .orElseThrow(() -> new EntityNotFoundException("존재하지 않는 회원입니다"));

        // 프로필 이미지 처리 - 이미지가 있으면 member의 profileImage, 없으면 기본 이미지 사용
        String profileImage = member.getProfileImage() != null && !member.getProfileImage().isEmpty()
                ? member.getProfileImage()
                : "https://cdn.pixabay.com/photo/2025/05/10/15/40/bear-9591466_1280.jpg";

        List<QuestionAnswer> questionAnswer = questionAnswerRepository.findByMember(member);
        List<MemberProfileResponseDTO.QuestionsDTO> questionAnswerDTOs = questionAnswer.stream()
                .map(qa -> new MemberProfileResponseDTO.QuestionsDTO(qa.getQuestion(), qa.getAnswer()))
                .toList();

        Long minId = Math.min(requestMemberId, targetMemberId);
        Long maxId = Math.max(requestMemberId, targetMemberId);
        boolean isFriend = friendRepository.existsByMemberAAndMemberBAndStatus(minId, maxId, FriendStatus.ACTIVE);

        FriendRequest friendRequest = null;
        boolean friendRequestSentByMe = false;
        boolean friendRequestReceivedFromOther = false;

        if (!isFriend) {
            friendRequest = friendRequestRepository.findFriendRequestBetween(requestMemberId, targetMemberId)
                    .orElse(null);

            if (friendRequest != null) {
                if (friendRequest.getSenderId().equals(requestMemberId)) {
                    friendRequestSentByMe = true;
                } else if (friendRequest.getSenderId().equals(targetMemberId)) {
                    friendRequestReceivedFromOther = true;
                }
            }
        }

        return MemberProfileResponseDTO.builder()
                .id(member.getId())
                .nickname(member.getNickname())
                .profileImage(profileImage) // 수정된 부분
                .gender(member.getGender())
                .location("KOREA") // TODO: 실제 위치 정보로 변경 필요
                .mbti(member.getMbti())
                .statusMessage(member.getStatusMessage())
                .questionAnswers(questionAnswerDTOs)
                .interests(member.getInterest())
                .isFriend(isFriend)
                .temperature(43.7) // TODO: 실제 온도 계산 로직 추가 필요
                .friendRequestSentByMe(friendRequestSentByMe)
                .friendRequestReceivedFromOther(friendRequestReceivedFromOther)
                .likes(24) // TODO: 실제 좋아요 수 조회 로직 추가 필요
                .build();
    }


    //회원가입
    @Transactional
    public MemberResponseDTO signUp(SignUpRequestDTO signUpRequestDTO) {

        if(checkUsername(signUpRequestDTO.getUsername())){
            throw new EntityNotFoundException("이미 사용중인 아이디 입니다.");
        }
        if(checkNickname(signUpRequestDTO.getNickname())){
            throw new EntityNotFoundException("이미 사용중인 닉네임입니다.");
        }

        String[] basicProfiles = new String[] {
                "https://beta-hive.s3.ap-northeast-2.amazonaws.com/user-upload/a3f15142-80fc-4039-a2a2-30aa16dc26f8_1750153596579.png",
                "https://beta-hive.s3.ap-northeast-2.amazonaws.com/user-upload/d44a9dc8-57b6-4112-a0dd-441b26481ef5_1750153599681.png"
        };
        String randomProfileImage = basicProfiles[new Random().nextInt(basicProfiles.length)];

        DeviceInfo deviceInfo =DeviceInfo
                .builder()
                .deviceId(signUpRequestDTO.getDeviceInfo().getDeviceId())
                .deviceModel(signUpRequestDTO.getDeviceInfo().getDeviceModel())
                .systemName(signUpRequestDTO.getDeviceInfo().getSystemName())
                .osVersion(signUpRequestDTO.getDeviceInfo().getOsVersion())
                .build();

        Member member = Member
                .builder()
                .username(signUpRequestDTO.getUsername())
                .phoneNumber(signUpRequestDTO.getPhoneNumber())
                .password(passwordEncoder.encode(signUpRequestDTO.getPassword()))
                .deviceInfo(deviceInfo)
                .nickname(signUpRequestDTO.getNickname())
                .gender(signUpRequestDTO.getGender())
                .birthDate(signUpRequestDTO.getBirthDate())
                .age(Member.calculateAge(signUpRequestDTO.getBirthDate()))
                .mbti(signUpRequestDTO.getMbti())
                .profileImage(randomProfileImage) // 여기에 랜덤 이미지 적용
                .interest(signUpRequestDTO.getInterests())
                .build();

        memberRepository.save(member);

        RequiredConsent requiredConsent = RequiredConsent
                .builder()
                .memberId(member.getId())
                .termsOfService(true)
                .privacyPolicy(true)
                .build();

        requiredConsentRepository.save(requiredConsent);


        return new MemberResponseDTO(member);
    }


    //로그인
    @Transactional
    public MemberResponseDTO signIn(SignInRequestDTO signInRequestDTO, HttpServletRequest request, HttpServletResponse response) {
        try {
            String username = signInRequestDTO.getUsername();

            UsernamePasswordAuthenticationToken authenticationToken =
                    new UsernamePasswordAuthenticationToken(username, signInRequestDTO.getPassword());

            Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

            jwtUtil.createTokenAndSaved(authentication, response, request);

            CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();
            Member member = customUserDetails.member();

            return new MemberResponseDTO(member);

        } catch (BadCredentialsException e) {
            throw new AuthException(ErrorCode.INVALID_PASSWORD);
        } catch (UsernameNotFoundException e) {
            throw new MemberException(ErrorCode.MEMBER_NOT_FOUND);
        } catch (AuthenticationException e) {
            throw new AuthException(ErrorCode.UNAUTHORIZED, "인증 처리 중 오류가 발생했습니다.");
        }
    }

    //아이디중복체크
    @Transactional(readOnly = true)
    public boolean checkUsername(String username){
        return memberRepository.existsByUsername(username);
    }

    //닉네임중복체크
    @Transactional(readOnly = true)
    public boolean checkNickname(String nickname){
        return memberRepository.existsByNickname(nickname);
    }

    @Transactional
    public void updateNickname(CustomUserDetails userDetails, String nickname) {

        Member member = userDetails.member();
        member.updateNickname(nickname);
    }

    @Transactional
    public void updateMbti(CustomUserDetails userDetails, MBTI mbti) {

        Member member = userDetails.member();
        member.updateMbti(mbti);
    }

    @Transactional
    public void updateMessage(CustomUserDetails userDetails, String message) {

        Member member = userDetails.member();
        member.updateMessage(message);
    }
    @Transactional
    public void updateProfileImage(CustomUserDetails userDetails, String imageUrl) {
        System.out.println(imageUrl);

        Member member = userDetails.member();
        member.updateProfileImage(imageUrl);
    }


    @Transactional
    public void updateInterests(CustomUserDetails userDetails, MemberRequest.MemberInterests interests) {
        Member member = userDetails.member();
        member.updateInterests(interests);
    }

    @Transactional
    public void updateQuestions(CustomUserDetails userDetails, MemberRequest.QuestionAnswerRequests questionAnswerRequests) {
        Member member = userDetails.member();

        // 기존 질문 답변들 삭제(필요하면)
        questionAnswerRepository.deleteByMember(member);

        // 새로운 질문 답변 저장
        questionAnswerRequests.questions().stream()
                .map(q -> QuestionAnswer.builder()
                        .answer(q.answer())
                        .question(q.question())
                        .member(member)
                        .build())
                .forEach(questionAnswerRepository::save);
    }


    @Transactional(readOnly = true)
    public MyInterestsResponseDTO getMyInterest(Member member) {
        List<String> interests = member.getInterest();
        return MyInterestsResponseDTO.builder()
                .interests(interests)
                .build();
    }


    @Transactional(readOnly = true)
    public MemberResponse.Username findUsername(@NotBlank String number) {
        Member member = getMemberByNumber(number);

        return new MemberResponse.Username(member.getUsername());
    }



    @Transactional
    public void resetPassword(MemberRequest.ResetPassword reset) {
        Member member =memberRepository.findByUsername(reset.username()).orElseThrow(()->new EntityNotFoundException("해당 회원이 존재하지 않습니다."));
        String encodedPassword = passwordEncoder.encode(reset.password());
        member.setPassword(encodedPassword);

    }
    @Transactional
    public boolean verifyUserinfo(MemberRequest.VerifyUserInfo verify) {
        Member member =getMemberByNumber(verify.phoneNumber());

        return member.getUsername().equals(verify.username());

    }


    private Member getMemberByNumber(String number) {
        return memberRepository.findByPhoneNumber(number)
                .orElseThrow(() -> new EntityNotFoundException("해당 회원이 존재하지 않습니다."));
    }
    private Member findMemberById(Long memberId) {
        return memberRepository.findById(memberId).
                orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
    }
}
