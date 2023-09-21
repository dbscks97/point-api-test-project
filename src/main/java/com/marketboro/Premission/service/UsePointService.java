package com.marketboro.Premission.service;

import com.marketboro.Premission.dto.PointDto;

import java.util.concurrent.CompletableFuture;

public interface UsePointService {
    CompletableFuture<PointDto.UsePointResponse> usePointsAsync(Long memberId, String memberName, int pointsToUse);
}
