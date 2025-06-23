package random.call.global.sms;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor
@Getter
public class SmsVerify {

    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String phoneNumber; // PK로 사용

    private String verifyCode;


    @Builder
    public SmsVerify(String phoneNumber, String verifyCode) {
        this.phoneNumber = phoneNumber;
        this.verifyCode = verifyCode;
    }
}
