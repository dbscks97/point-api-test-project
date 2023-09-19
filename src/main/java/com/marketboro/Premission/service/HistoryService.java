package com.marketboro.Premission.service;

import com.marketboro.Premission.entity.History;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface HistoryService {
    void scheduleExpirePoints();
    void expirePoints();
    Page<History> getPagedUsageHistoryByMemberId(Long memberId, String memberName, Pageable pageable);
}
