package com.marketboro.Premission.controller;

import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.enums.MemberErrorResult;
import com.marketboro.Premission.exception.MemberException;
import com.marketboro.Premission.response.MemberResponse;
import com.marketboro.Premission.service.HistoryServiceImpl;
import com.marketboro.Premission.service.MemberServiceImpl;
import com.marketboro.Premission.service.AccruePointServiceImpl;
import com.marketboro.Premission.service.UsePointServiceImpl;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.ExecutionException;

import static com.marketboro.Premission.controller.MemberConstants.MEMBER_ID_HEADER;

@RestController
public class MemberController {
    private final MemberServiceImpl memberServiceImpl;
    private final AccruePointServiceImpl accruePointService;
    private final UsePointServiceImpl usePointService;
    private final HistoryServiceImpl historyServiceImpl;

    @Autowired
    public MemberController(MemberServiceImpl memberServiceImpl, AccruePointServiceImpl accruePointService, UsePointServiceImpl usePointService, HistoryServiceImpl historyServiceImpl) {
        this.memberServiceImpl = memberServiceImpl;
        this.accruePointService = accruePointService;
        this.usePointService = usePointService;
        this.historyServiceImpl = historyServiceImpl;
    }

    //회원별 적립금 합계 조회 API
    @GetMapping("/api/v1/{memberId}/points")
    public ResponseEntity<?> getPoints(@RequestHeader(MEMBER_ID_HEADER) String memberName, @PathVariable Long memberId) {
        if (memberId == null) {
            return ResponseEntity.badRequest().build();
        }


        try {
            MemberResponse response = memberServiceImpl.getPoints(memberId, memberName);
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
        Page<History> historyPage = historyServiceImpl.getPagedUsageHistoryByMemberId(memberId, memberName, PageRequest.of(page, size));
        return ResponseEntity.ok(historyPage);
    }



    //회원별 적립금 사용 API
    @PostMapping("/api/v1/{memberId}/use")
    public ResponseEntity<Void> usePoints(
            @RequestHeader(MEMBER_ID_HEADER) String memberName,
            @PathVariable Long memberId,
            @RequestParam int pointsToUse) {


            usePointService.usePointsAsync(memberId, memberName, pointsToUse);
            return ResponseEntity.noContent().build();
    }

    //회원별 적립금 취소 API
//    @PostMapping("/{memberId}/cancel")
//    public void cancelPoints(@PathVariable Long memberId, @RequestParam int pointsToCancel) {
//        accruePointService.cancelPointsAsync(memberId, pointsToCancel);
//    }
}
