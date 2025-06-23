package random.call.global.sms;

import lombok.RequiredArgsConstructor;
import net.nurigo.sdk.message.model.Balance;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.Map;
import java.util.Random;

@RestController
@RequestMapping("/api/v1/sms")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;
    private final SmsVerifyRepository smsVerifyRepository;
    private final String SMS_SENDER_NUMBER = "01088712301"; // 등록된 발신번호

    /**
     * 인증번호 발송 API
     */
    @PostMapping("/send-verification")
    public ResponseEntity<Map<String, Object>> sendVerificationSms(
            @RequestBody Map<String, String> request) {

        String phoneNumber = request.get("phoneNumber");

        // 1. 휴대폰 번호 유효성 검사
        if (!phoneNumber.matches("^010\\d{8}$")) {
            throw new IllegalArgumentException("유효하지 않은 휴대폰 번호 형식입니다.");
        }

        // 2. 인증번호 생성 (6자리 난수)
        String authCode = String.format("%06d", new Random().nextInt(999999));

        // 3. DB 저장 (기존 번호 있으면 덮어쓰기)
        SmsVerify smsVerify = SmsVerify.builder()
                .phoneNumber(phoneNumber)
                .verifyCode(authCode)
                .build();
        smsVerifyRepository.save(smsVerify); // phoneNumber가 PK이므로 자동 업데이트

        // 4. SMS 발송
        String message = "[Tello] 인증번호: " + authCode;
        SingleMessageSentResponse smsResponse = messageService.sendSms(
                SMS_SENDER_NUMBER,
                phoneNumber,
                message
        );

        // 5. 응답
        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "인증번호가 발송되었습니다",
                "data", smsResponse
        ));
    }


    @PostMapping("/send-verification/dev")
    public ResponseEntity<Boolean> sendVerificationSmsDev(
            @RequestBody Map<String, String> request) {

        String phoneNumber = request.get("phoneNumber");

        // 1. 휴대폰 번호 유효성 검사
        if (!phoneNumber.matches("^010\\d{8}$")) {
            throw new IllegalArgumentException("유효하지 않은 휴대폰 번호 형식입니다.");
        }

        // 2. 인증번호 생성 (6자리 난수)
        String authCode = "111111";

        // 3. DB 저장 (기존 번호 있으면 덮어쓰기)
        SmsVerify smsVerify = SmsVerify.builder()
                .phoneNumber(phoneNumber)
                .verifyCode(authCode)
                .build();
        smsVerifyRepository.save(smsVerify); // phoneNumber가 PK이므로 자동 업데이트
        // 5. 응답
        return ResponseEntity.ok(true);
    }

    /**
     * 인증번호 검증 API
     */
    @PostMapping("/verify-code")
    public ResponseEntity<Map<String, Object>> verifyCode(
            @RequestBody Map<String, String> request) {

        String phoneNumber = request.get("phoneNumber");
        String inputCode = request.get("code");

        // 1. 인증번호 조회
        SmsVerify smsVerify = smsVerifyRepository.findById(phoneNumber)
                .orElseThrow(() -> new IllegalArgumentException("인증번호를 먼저 발송해주세요."));

        // 2. 인증번호 비교
        boolean isValid = smsVerify.getVerifyCode().equals(inputCode);

        // 3. 응답
        if (isValid) {
            // 인증 성공 시 삭제 (선택사항)
            smsVerifyRepository.deleteById(phoneNumber);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "message", "인증이 완료되었습니다"
            ));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                    "success", false,
                    "message", "인증번호가 일치하지 않습니다"
            ));
        }
    }
}