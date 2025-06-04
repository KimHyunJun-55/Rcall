package random.call.domain.report.type;

import lombok.Getter;

public enum PostReportTitleType {
    AD_CONTENT(1, "광고성 게시물"),
    SEXUAL_CONTENT(2, "선정적 내용 포함"),
    ABUSIVE_LANGUAGE(3, "욕설 또는 모욕적 언행"),
    FALSE_INFO(4, "허위 정보 게시"),
    OTHER(5, "기타 부적절한 내용");

    private final int id;
    @Getter
    private final String label;


    PostReportTitleType(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public static PostReportTitleType fromId(int id) {
        for (PostReportTitleType type : values()) {
            if (type.id == id) return type;
        }
        throw new IllegalArgumentException("Invalid post report title id: " + id);
    }

}