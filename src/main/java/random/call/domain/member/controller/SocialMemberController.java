package random.call.domain.member.controller;


import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import random.call.domain.member.dto.social.SocialLoginResponse;
import random.call.domain.member.dto.social.SocialMemberRequest;
import random.call.domain.member.service.SocialMemberService;
import random.call.domain.member.type.SocialType;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/social")
public class SocialMemberController {

    private final SocialMemberService socialMemberService;

    @PostMapping("/kakao/sign-in")
    public ResponseEntity<?> socialMemberSignIn(@RequestBody SocialMemberRequest.KakaoId kakaoId, HttpServletRequest request, HttpServletResponse response){

        return ResponseEntity.ok(socialMemberService.socialLogin(kakaoId.kakaoId(), SocialType.KAKAO,request,response));
    }

    @PostMapping("/kakao/sign-up")
    public ResponseEntity<SocialLoginResponse> socialMemberSignUp(@RequestBody SocialMemberRequest.SocialSignUpRequest signUpRequest, HttpServletRequest request, HttpServletResponse response){

        return ResponseEntity.ok(socialMemberService.signUp(signUpRequest,request,response));
    }

    @PostMapping("google/sign-in")
    public ResponseEntity<SocialLoginResponse> googleMemberSignIn(@RequestBody SocialMemberRequest.GoogleToken token, HttpServletRequest request,HttpServletResponse response) throws JsonProcessingException {

        return ResponseEntity.ok(socialMemberService.getMemberInfoToGoogle(token.token(),request,response));
    }

//    @PostMapping("google/sign-up")
//    public ResponseEntity<SocialLoginResponse> googleMemberSignUp(){
//
//    }


}
