package com.marketboro.Premission.controller;

import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.service.MemberService;
import com.marketboro.Premission.service.PointService;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/members")
public class MemberController {
    private final MemberService memberService;
    private final PointService pointService;

    @Autowired
    public MemberController(MemberService memberService, PointService pointService) {
        this.memberService = memberService;
        this.pointService = pointService;
    }

    @GetMapping("/{memberId}/points")
    public int getRewardPoints(@PathVariable Long memberId) {
        return memberService.getRewardPointsByMemberId(memberId);
    }

    @GetMapping("/{memberId}/histories")
    public List<History> getHistories(@PathVariable Long memberId) {
        return memberService.getHistoriesByMemberId(memberId);
    }

    @PostMapping("/{memberId}/accrue")
    public void accruePoints(@PathVariable Long memberId, @RequestParam @Min(1) int points) {
        if (points <= 0) {
            throw new IllegalArgumentException("적립 포인트는 1 이상이어야 합니다.");
        }
        pointService.accruePointsAsync(memberId, points);
    }

    @PostMapping("/{memberId}/use")
    public void usePoints(@PathVariable Long memberId, @RequestParam int pointsToUse) {
        pointService.usePointsAsync(memberId, pointsToUse);
    }

    @PostMapping("/{memberId}/cancel")
    public void cancelPoints(@PathVariable Long memberId, @RequestParam int pointsToCancel) {
        pointService.cancelPointsAsync(memberId, pointsToCancel);
    }
}
