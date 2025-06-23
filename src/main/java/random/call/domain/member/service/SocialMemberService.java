package random.call.domain.member.service;


import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.hibernate.procedure.ParameterTypeException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import random.call.domain.member.Member;
import random.call.domain.member.RequiredConsent;
import random.call.domain.member.SocialMember;
import random.call.domain.member.dto.social.SocialLoginResponse;
import random.call.domain.member.dto.social.SocialMemberInfoDTO;
import random.call.domain.member.dto.social.SocialMemberRequest;
import random.call.domain.member.repository.MemberRepository;
import random.call.domain.member.repository.RequiredConsentRepository;
import random.call.domain.member.repository.SocialMemberRepository;
import random.call.domain.member.type.Gender;
import random.call.domain.member.type.MemberType;
import random.call.domain.member.type.SocialType;
import random.call.global.jwt.JwtUtil;
import random.call.global.security.userDetails.CustomUserDetails;

import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor

public class SocialMemberService {

    private final MemberRepository memberRepository;
    private final SocialMemberRepository socialMemberRepository;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final RequiredConsentRepository requiredConsentRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @Transactional
    public SocialLoginResponse socialLogin(String socialId,SocialType socialType, HttpServletRequest request, HttpServletResponse response) {
        Optional<SocialMember> socialMember= socialMemberRepository.findBySocialId(socialType.toString()+"_"+socialId);

        if (socialMember.isEmpty()) {
            return new SocialLoginResponse(false, null, null,null,null,null,null); // 아직 회원가입 안된 경우
        }else{
            return memberRepository.findById(socialMember.get().getMemberId())
                    .map(member -> signIn(member, request, response))
                    .orElseThrow(()->new EntityNotFoundException("해당 회원을 찾을 수 없습니다."));

        }
    }

    @Transactional
    public SocialLoginResponse signIn(Member member,HttpServletRequest request, HttpServletResponse response) {

        return getSocialLoginResponse(member, request, response);

    }

    @Transactional
    public SocialLoginResponse signUp(SocialMemberRequest.SocialSignUpRequest signUpRequest, HttpServletRequest request, HttpServletResponse response) {

        String social = signUpRequest.socialType() + "_" + signUpRequest.socialId();


        Member member= Member
                .builder()
                .nickname(signUpRequest.nickname())
                .username(social)
                .password(passwordEncoder.encode(UUID.randomUUID().toString()))
                .mbti(signUpRequest.mbti())
                .profileImage(signUpRequest.profileImage())
                .gender(signUpRequest.gender())
                .phoneNumber(signUpRequest.phoneNumber())
                .birthDate(signUpRequest.birthDate())
                .age(Member.calculateAge(signUpRequest.birthDate()))
                .interest(signUpRequest.interests())
                .memberType(MemberType.SOCIAL)
                .build();

        memberRepository.save(member);

        SocialMember socialMember = SocialMember
                .builder()
                .socialType(signUpRequest.socialType())
                .socialId(social)
                .memberId(member.getId())
                .build();
        socialMemberRepository.save(socialMember);

        RequiredConsent requiredConsent = RequiredConsent
                .builder()
                .memberId(member.getId())
                .termsOfService(true)
                .privacyPolicy(true)
                .build();

        requiredConsentRepository.save(requiredConsent);

        return getSocialLoginResponse(member, request, response);

    }

    private SocialLoginResponse getSocialLoginResponse(Member member, HttpServletRequest request, HttpServletResponse response) {
        Authentication authentication = forceLogin(member);

        jwtUtil.createTokenAndSaved(authentication, response, request);

        return new SocialLoginResponse(true, member.getId(), member.getNickname(), member.getCreatedAt(), member.getAge(),member.getGender(),member.getIsSubscriber());
    }

    private Authentication forceLogin(Member member) {

        CustomUserDetails userDetails = new CustomUserDetails(member);

        Authentication authentication = new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);

        return authentication;

    }

    @Transactional
    public SocialLoginResponse getMemberInfoToGoogle(String accessToken,
                                                     HttpServletRequest requests,
                                                     HttpServletResponse response) throws JsonProcessingException {

        String profile = "https://www.googleapis.com/oauth2/v3/userinfo";
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        HttpEntity<String> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        // ✅ GET 요청으로 수정
        ResponseEntity<String> responseEntity = restTemplate.exchange(
                profile,
                HttpMethod.GET,
                request,
                String.class
        );

        String responseBody = responseEntity.getBody();
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode jsonNode = objectMapper.readTree(responseBody);

        String googleId = jsonNode.get("sub").asText(); // 구글 고유 ID
        return socialLogin(googleId, SocialType.GOOGLE, requests, response);
    }



}
