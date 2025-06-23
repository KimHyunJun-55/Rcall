package random.call.domain.member;

import jakarta.persistence.Convert;
import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import random.call.global.encrypt.CryptoConverter;

@Embeddable
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeviceInfo {
    @Convert(converter = CryptoConverter.class)
    private String deviceId;

    @Convert(converter = CryptoConverter.class)
    private String osVersion;

    private String deviceModel;

    private String systemName;
}
