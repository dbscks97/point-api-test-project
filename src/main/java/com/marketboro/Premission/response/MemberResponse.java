package com.marketboro.Premission.response;


import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
public class MemberResponse {
    private final Long memberId;
    private final String memberName;
    private final Integer rewardPoints;
}
