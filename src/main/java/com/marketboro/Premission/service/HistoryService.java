package com.marketboro.Premission.service;

import com.marketboro.Premission.dto.HistoryDto;
import com.marketboro.Premission.entity.History;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface HistoryService {
    void scheduleExpirePoints();
    void expirePoints();

    List<History> getPagedUsageHistoryByMemberId(Long memberId, String memberName, Pageable pageable);
}
