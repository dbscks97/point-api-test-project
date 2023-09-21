package com.marketboro.Premission.service;

import com.marketboro.Premission.dto.PointDto;
import com.marketboro.Premission.response.MemberResponse;

public interface MemberService {
    PointDto.GetPointsResponse getPoints(Long memberId, String memberName);
}