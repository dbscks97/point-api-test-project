package com.marketboro.Premission.dto;

import lombok.*;

import java.util.Date;
import java.util.List;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class HistoryDto {

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class Response {
        private List<PointHistory> history;
    }

    @Getter
    @AllArgsConstructor(staticName = "of")
    public static class PointHistory {
        public Long historyId;
        public int points;
        public String type;
        public Date createdAt;
        public Date updatedAt;
    }
}