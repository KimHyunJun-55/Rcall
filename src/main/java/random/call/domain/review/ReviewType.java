package random.call.domain.review;

import lombok.Getter;
import random.call.domain.report.type.CallReportTitleType;

public enum ReviewType {

    KIND(1, "친절했어요"),
    COMMUNICATION(2, "의사소통이 잘 됐어요"),
    WARM(3, "다정했어요"),
    THOUGHTFUL(4, "생각이 깊었어요"),
    OTHER(5, "기타");

    private final int id;
    @Getter
    private final String label;


    ReviewType(int id, String label) {
        this.id = id;
        this.label = label;
    }
    public static ReviewType fromId(int id) {
        for (ReviewType type : values()) {
            if (type.id == id) return type;
        }
        throw new IllegalArgumentException("Invalid review title id: " + id);
    }
}
