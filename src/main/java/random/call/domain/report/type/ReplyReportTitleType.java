package random.call.domain.report.type;

import lombok.Getter;

public enum ReplyReportTitleType {
    INAPPROPRIATE_LANGUAGE(1, "욕설 또는 혐오 표현"),
    SEXUAL_CONTENT(2, "성적 발언 또는 행동"),
    SCAM(3, "사기나 금전 요구"),
    ILLEGAL_BEHAVIOR(4, "불법 행위 시도"),
    PERSONAL_INFO(5, "개인정보 요구 또는 노출"),
    OTHER(6, "기타 문제 행동");

    private final int id;
    @Getter
    private final String label;


    ReplyReportTitleType(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public static ReplyReportTitleType fromId(int id) {
        for (ReplyReportTitleType type : values()) {
            if (type.id == id) return type;
        }
        throw new IllegalArgumentException("Invalid reply report title id: " + id);
    }

}