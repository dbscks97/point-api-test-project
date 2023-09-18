package com.marketboro.Premission.request;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
@NoArgsConstructor(force = true)
public class MemberRequest {
    private final Long memberId;
    private final String memberName;
    private final Integer rewardPoints;

}
