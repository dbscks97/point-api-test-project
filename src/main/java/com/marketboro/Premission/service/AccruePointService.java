package com.marketboro.Premission.service;

import java.util.concurrent.CompletableFuture;

public interface AccruePointService {
     CompletableFuture<Void> accruePointsAsync(Long memberId, String memberName,int points);
}
