package random.call.domain.member;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import random.call.global.encrypt.CryptoConverter;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeleteMemberInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Convert(converter = CryptoConverter.class)
    private String encryptedId;

    @Convert(converter = CryptoConverter.class)
    private String encryptedDeviceId;

    @Convert(converter = CryptoConverter.class)
    private String encryptedBirth;

    private LocalDateTime deletedAt;

    @Builder
    public DeleteMemberInfo(String encryptedId, String encryptedDeviceId, String encryptedBirth, LocalDateTime deletedAt) {
        this.encryptedId = encryptedId;
        this.encryptedDeviceId = encryptedDeviceId;
        this.encryptedBirth = encryptedBirth;
        this.deletedAt = deletedAt;
    }



}
