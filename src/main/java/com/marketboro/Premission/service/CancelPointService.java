package com.marketboro.Premission.service;

import java.util.concurrent.CompletableFuture;

public interface CancelPointService {
    CompletableFuture<Void> cancelPointsAsync(Long memberId, int pointsToCancel);
}
