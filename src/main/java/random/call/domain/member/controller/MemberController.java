package random.call.domain.member.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import random.call.domain.member.dto.*;
import random.call.domain.member.service.MemberService;
import random.call.global.security.userDetails.CustomUserDetails;
import random.call.global.security.userDetails.JwtUserDetails;

@RestController
@RequestMapping("/api/v1/member")
@RequiredArgsConstructor
@Slf4j
public class MemberController {

    private final MemberService memberService;

    //멤버단일조회
    @GetMapping("")
    public ResponseEntity<MemberResponseDTO> readOneMember(@AuthenticationPrincipal JwtUserDetails jwtUserDetails){
        Long memberId = jwtUserDetails.id();

        MemberResponseDTO memberResponseDTO = memberService.getMember(memberId);

        return ResponseEntity.ok(memberResponseDTO);
    }

    //토큰소지자의 관심사가져오기
    @GetMapping("/interests")
    public ResponseEntity<MyInterestsResponseDTO> getMyInterest(
            @AuthenticationPrincipal CustomUserDetails customUserDetails) {
        MyInterestsResponseDTO responseDTO = memberService.getMyInterest(customUserDetails.member());

        return ResponseEntity.ok(responseDTO);
    }


    //해당유저의 프로필 조회
    @GetMapping("/{memberId}")
    public ResponseEntity<MemberProfileResponseDTO> getProfileById(
            @PathVariable("memberId") Long targetMemberId,
            @AuthenticationPrincipal JwtUserDetails jwtUserDetails){

        Long memberId = jwtUserDetails.id();

        MemberProfileResponseDTO memberResponseDTO = memberService.getProfileById(memberId,targetMemberId);

        return ResponseEntity.ok(memberResponseDTO);
    }

//    @PostMapping("/check-nickname")
//    public CommonResponse<Boolean> checkNickname(@RequestBody)

    //회원가입
    @PostMapping("/sign-up")
    public ResponseEntity<MemberResponseDTO> signUp(@RequestBody SignUpRequestDTO signUpRequestDTO){

        MemberResponseDTO memberResponseDTO = memberService.signUp(signUpRequestDTO);

        log.info("회원가입");
        return ResponseEntity.ok(memberResponseDTO);

    }

    //로그인
    @PostMapping("/sign-in")
    public ResponseEntity<MemberResponseDTO> signIn(
            @RequestBody SignInRequestDTO signInRequestDTO,
            HttpServletRequest request,
            HttpServletResponse response
    ){
        MemberResponseDTO memberResponseDTO = memberService.signIn(signInRequestDTO,request,response);
        log.info("로그인");
        return ResponseEntity.ok(memberResponseDTO);

    }

    @PostMapping("/check-nickname")
    public ResponseEntity<Boolean> checkNickname(@RequestBody MemberRequest.CheckNickname checkNickname){
        boolean check = memberService.checkNickname(checkNickname.nickname());
        return ResponseEntity.ok(check);

    }
    @PostMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestBody MemberRequest.CheckUsername checkNickname){
        boolean check = memberService.checkUsername(checkNickname.username());
        return ResponseEntity.ok(check);

    }

    //프로필수정
    @PatchMapping("/nickname")
    public ResponseEntity<?> updateNickname(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody MemberRequest.CheckNickname nickname){

        memberService.updateNickname(userDetails,nickname.nickname());

        return ResponseEntity.ok(true);
    }
    @PatchMapping("/message")
    public ResponseEntity<?> updateMessage(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody MemberRequest.Message message){

        memberService.updateMessage(userDetails,message.message());

        return ResponseEntity.ok(true);
    }

    @PatchMapping("/profileImage")
    public ResponseEntity<?> updateProfileImage(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody MemberRequest.ProfileImage imageUrl){

        memberService.updateProfileImage(userDetails,imageUrl.imageUrl());

        return ResponseEntity.ok(true);
    }

    @PutMapping("/interests")
    public ResponseEntity<?> updateInterests(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody MemberRequest.MemberInterests interests){

        memberService.updateInterests(userDetails,interests);

        return ResponseEntity.ok(true);

    }
    @PutMapping("/questions")
    public ResponseEntity<?> updateQuestions(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody MemberRequest.QuestionAnswerRequests questionAnswerRequests){

        memberService.updateQuestions(userDetails,questionAnswerRequests);

        return ResponseEntity.ok(true);

    }
    @GetMapping("/token")
    public ResponseEntity<String> validToken() {
        // 토큰이 유효하다면 200 OK 반환
        log.info("Token is valid");
        return new ResponseEntity<>("Token is valid", HttpStatus.OK);
    }






}
