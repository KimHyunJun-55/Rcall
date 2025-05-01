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
import random.call.domain.member.Member;
import random.call.domain.member.MemberRepository;
import random.call.domain.member.dto.MemberRequest;
import random.call.domain.member.dto.MemberResponseDTO;
import random.call.domain.member.dto.SignInRequestDTO;
import random.call.domain.member.dto.SignUpRequestDTO;
import random.call.global.jwt.JwtUtil;
import random.call.global.security.userDetails.CustomUserDetails;
import random.call.global.security.userDetails.JwtUserDetails;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManagerBuilder authenticationManagerBuilder;
    private final JwtUtil jwtUtil;

    //멤버조회
    @Transactional(readOnly = true)
    public MemberResponseDTO getMember(JwtUserDetails userDetails) {

        Member member = memberRepository.findById(userDetails.id()).
                orElseThrow(()->new EntityNotFoundException("없는 회원입니다"));
        return new MemberResponseDTO(member);
    }

    //회원가입
    @Transactional
    public MemberResponseDTO signUp(SignUpRequestDTO signUpRequestDTO) {

        if(checkUsername(signUpRequestDTO.getUsername())){
            throw new EntityNotFoundException("정보가 없습니다.");
        }
        if(checkNickname(signUpRequestDTO.getNickname())){
            throw new EntityNotFoundException("정보가 없습니다.");
        }

        Member member = Member
                .builder()
                .username(signUpRequestDTO.getUsername())
                .password(passwordEncoder.encode(signUpRequestDTO.getPassword()))
                .nickname(signUpRequestDTO.getNickname())
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
}
