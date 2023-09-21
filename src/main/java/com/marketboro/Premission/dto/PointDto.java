package com.marketboro.Premission.dto;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class PointDto {

    @Getter
    @Setter
    public static class GetPointsResponse{
        private int points;
    }
    @Getter
    @Setter
    public static class UsePointResponse{
        private int pointsUsed;
        private int deductPointNo;
    }

    @Getter
    @Setter
    public static class CancelPointResponse{
        private int pointCanceled;
        private int deductPointNo;
    }
}
