package random.call.domain.member.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public  class DeviceInfoDTO{
    private String deviceId;
    private String osVersion;
    private String deviceModel;
    private String systemName;

}