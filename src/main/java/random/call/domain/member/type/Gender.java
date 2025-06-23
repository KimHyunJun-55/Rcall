package random.call.domain.member.type;

public enum Gender {
    FEMALE,  // 여성
    MALE,    // 남성
    ANY;      // 무관
    // 문자열 → Enum 변환 (대소문자 불문)
    public static Gender fromString(String value) {
        if (value == null) return ANY;
        return switch (value.toUpperCase()) {
            case "FEMALE", "여성" -> FEMALE;
            case "MALE", "남성" -> MALE;
            default -> ANY; // "무관" 또는 기타 값
        };
    }
    }