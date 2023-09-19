package com.marketboro.Premission.service;

import java.util.concurrent.CompletableFuture;

public interface UsePointService {
    CompletableFuture<Void> usePointsAsync(Long memberId, int pointsToUse);
}
