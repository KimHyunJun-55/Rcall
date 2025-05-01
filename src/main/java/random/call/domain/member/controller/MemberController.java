package random.call.domain.member.controller;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import random.call.domain.member.dto.MemberRequest;
import random.call.domain.member.dto.MemberResponseDTO;
import random.call.domain.member.dto.SignInRequestDTO;
import random.call.domain.member.dto.SignUpRequestDTO;
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
    public ResponseEntity<MemberResponseDTO> readOneMember(@AuthenticationPrincipal JwtUserDetails userDetails){

        MemberResponseDTO memberResponseDTO = memberService.getMember(userDetails);

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
        return ResponseEntity.ok(!check);

    }
    @PostMapping("/check-username")
    public ResponseEntity<Boolean> checkUsername(@RequestBody MemberRequest.CheckUsername checkNickname){
        boolean check = memberService.checkUsername(checkNickname.username());
        return ResponseEntity.ok(!check);

    }

    @PutMapping("/nickname")
    public ResponseEntity<?> updateNickname(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody MemberRequest.CheckNickname checkNickname){

        memberService.updateNickname(userDetails,checkNickname);

        return ResponseEntity.ok(true);

    }




}
