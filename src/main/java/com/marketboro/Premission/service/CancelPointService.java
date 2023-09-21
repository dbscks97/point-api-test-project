package com.marketboro.Premission.service;

import com.marketboro.Premission.dto.PointDto;

import java.util.concurrent.CompletableFuture;

public interface CancelPointService {
     CompletableFuture<PointDto.CancelPointResponse> cancelPointsAsync(Long memberId, String memberName, int pointsToCancel, int deductPointNo);
}
