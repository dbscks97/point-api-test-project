package com.marketboro.Premission.controller;

import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.enums.MemberErrorResult;
import com.marketboro.Premission.exception.MemberException;
import com.marketboro.Premission.response.MemberResponse;
import com.marketboro.Premission.service.HistoryService;
import com.marketboro.Premission.service.MemberService;
import com.marketboro.Premission.service.AccruePointService;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import static com.marketboro.Premission.controller.MemberConstants.MEMBER_ID_HEADER;

@RestController
public class MemberController {
    private final MemberService memberService;
    private final AccruePointService accruePointService;
    private final HistoryService historyService;

    @Autowired
    public MemberController(MemberService memberService, AccruePointService accruePointService, HistoryService historyService) {
        this.memberService = memberService;
        this.accruePointService = accruePointService;
        this.historyService = historyService;
    }

    //회원별 적립금 합계 조회 API
    @GetMapping("/api/v1/{memberId}/points")
    public ResponseEntity<?> getPoints(@RequestHeader(MEMBER_ID_HEADER) String memberName, @PathVariable Long memberId) {
        if (memberId == null) {
            return ResponseEntity.badRequest().build();
        }

        try {
            MemberResponse response = memberService.getPoints(memberId, memberName);
            return ResponseEntity.ok(response);
        } catch (MemberException e) {
            if (e.getErrorResult() == MemberErrorResult.MEMBER_NOT_FOUND) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Member Not Found");
            } else if (e.getErrorResult() == MemberErrorResult.NOT_MEMBER_OWNER) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Not Member Owner");
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error");
            }
        }
    }

    //회원별 적립금 적립 API
    @PostMapping("/api/v1/{memberId}/accrue")
    public ResponseEntity<Void> accruePoints(@RequestHeader(MEMBER_ID_HEADER) String memberName, @PathVariable Long memberId, @RequestParam @Min(1) int points) {

        accruePointService.accruePointsAsync(memberId, memberName,points);
        return ResponseEntity.noContent().build();
    }

    //회원별 적립금 적립/사용 내역 조회 API (페이징 처리)
    @GetMapping("/api/v1/{memberId}/point-history")
    public ResponseEntity<Page<History>> getPagedPointHistoryByMemberId(
            @RequestHeader(MEMBER_ID_HEADER) String memberName,
            @PathVariable Long memberId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<History> historyPage = historyService.getPagedUsageHistoryByMemberId(memberId, memberName, PageRequest.of(page, size));
        return ResponseEntity.ok(historyPage);
    }



    //회원별 적립금 사용 API
//    @PostMapping("/{memberId}/use")
//    public void usePoints(@PathVariable Long memberId, @RequestParam int pointsToUse) {
//        accruePointService.usePointsAsync(memberId, pointsToUse);
//    }

    //회원별 적립금 취소 API
//    @PostMapping("/{memberId}/cancel")
//    public void cancelPoints(@PathVariable Long memberId, @RequestParam int pointsToCancel) {
//        accruePointService.cancelPointsAsync(memberId, pointsToCancel);
//    }
}
