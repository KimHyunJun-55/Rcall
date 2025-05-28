package random.call.domain.member.service;

import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import random.call.domain.friendList.FriendRepository;
import random.call.domain.friendList.type.FriendStatus;
import random.call.domain.friendRequest.FriendRequest;
import random.call.domain.friendRequest.FriendRequestRepository;
import random.call.domain.member.Member;
import random.call.domain.member.repository.MemberRepository;
import random.call.domain.member.QuestionAnswer;
import random.call.domain.member.dto.*;
import random.call.domain.member.repository.QuestionAnswerRepository;
import random.call.global.jwt.JwtUtil;
import random.call.global.security.userDetails.CustomUserDetails;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final MemberRepository memberRepository;
    private final FriendRequestRepository friendRequestRepository;
    private final QuestionAnswerRepository questionAnswerRepository;

    private final PasswordEncoder passwordEncoder;
    private final FriendRepository friendRepository;
    private final JwtUtil jwtUtil;

    //멤버조회
    @Transactional(readOnly = true)
    public MemberResponseDTO getMember(Long memberId) {

        Member member = memberRepository.findById(memberId).
                orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
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

        Member member = Member
                .builder()
                .username(signUpRequestDTO.getUsername())
                .password(passwordEncoder.encode(signUpRequestDTO.getPassword()))
                .nickname(signUpRequestDTO.getNickname())
                .gender(signUpRequestDTO.getGender())
                .mbti(signUpRequestDTO.getMbti())
                .profileImage("null")
                .interest(signUpRequestDTO.getInterests())
                .build();

        memberRepository.save(member);


        return new MemberResponseDTO(member);
    }


    //로그인
    @Transactional
    public MemberResponseDTO signIn(SignInRequestDTO signInRequestDTO, HttpServletRequest request, HttpServletResponse response) {
        String username = signInRequestDTO.getUsername();

        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(username,signInRequestDTO.getPassword());

        Authentication authentication = authenticationManagerBuilder.getObject().authenticate(authenticationToken);

        jwtUtil.createTokenAndSaved(authentication,response,request);

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        Member member = customUserDetails.member();

        return new MemberResponseDTO(member);

    }

    //아이디중복체크
    public boolean checkUsername(String username){
        return memberRepository.existsByUsername(username);
    }

    //닉네임중복체크
    public boolean checkNickname(String nickname){
        return memberRepository.existsByNickname(nickname);
    }

    @Transactional
    public void updateNickname(CustomUserDetails userDetails, MemberRequest.CheckNickname checkNickname) {

        Member member = userDetails.member();
        member.updateNickname(checkNickname.nickname());
    }
    @Transactional
    public void updateMember(CustomUserDetails userDetails, MemberRequest.MemberInfo memberInfo) {
        Member member = userDetails.member();
        member.updateMember(memberInfo);
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


}
