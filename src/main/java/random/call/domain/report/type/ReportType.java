package random.call.domain.report.type;

public enum ReportType {
    INAPPROPRIATE_LANGUAGE(1, "부적절한 언어 사용"),
    SEXUAL_CONTENT(2, "성적 발언 또는 행동"),
    ABUSIVE_LANGUAGE(3, "욕설 또는 모욕적 언행"),
    SCAM_OR_ILLEGAL(4, "사기 또는 불법 행위 시도"),
    OTHER(5, "기타 문제 행동");

    private final int id;
    private final String label;

    ReportType(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public static ReportType fromId(int id) {
        for (ReportType type : values()) {
            if (type.id == id) return type;
        }
        throw new IllegalArgumentException("Invalid report type id: " + id);
    }
}
