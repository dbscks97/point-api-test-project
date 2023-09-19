package com.marketboro.Premission.service;

import com.marketboro.Premission.response.MemberResponse;

public interface MemberService {
    MemberResponse getPoints(Long memberId, String memberName);
}