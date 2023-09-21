package com.marketboro.Premission.controller;

import com.marketboro.Premission.dto.HistoryDto;
import com.marketboro.Premission.dto.PointDto;
import com.marketboro.Premission.mapper.PointHistoryDtoMapper;
import com.marketboro.Premission.response.CommonResponse;
import com.marketboro.Premission.response.ResponseCode;
import com.marketboro.Premission.service.*;
import jakarta.validation.constraints.Min;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.concurrent.CompletableFuture;

import static com.marketboro.Premission.controller.MemberConstants.MEMBER_ID_HEADER;

@RestController
public class MemberController {
    private final MemberServiceImpl memberServiceImpl;
    private final AccruePointServiceImpl accruePointService;
    private final UsePointServiceImpl usePointService;
    private final CancelPointServiceImpl cancelPointService;
    private final HistoryServiceImpl historyServiceImpl;
    private final PointHistoryDtoMapper pointHistoryDtoMapper;

    @Autowired
    public MemberController(MemberServiceImpl memberServiceImpl, AccruePointServiceImpl accruePointService, UsePointServiceImpl usePointService, CancelPointServiceImpl cancelPointService, HistoryServiceImpl historyServiceImpl, PointHistoryDtoMapper pointHistoryDtoMapper) {
        this.memberServiceImpl = memberServiceImpl;
        this.accruePointService = accruePointService;
        this.usePointService = usePointService;
        this.cancelPointService = cancelPointService;
        this.historyServiceImpl = historyServiceImpl;
        this.pointHistoryDtoMapper = pointHistoryDtoMapper;
    }

    //회원별 적립금 합계 조회 API
    @GetMapping("/api/v1/{memberId}/points")
    public ResponseEntity<CommonResponse<PointDto.GetPointsResponse>> getPoints(@RequestHeader(MEMBER_ID_HEADER) String memberName, @PathVariable Long memberId) {
        PointDto.GetPointsResponse pointsResponse = memberServiceImpl.getPoints(memberId, memberName);

        // CommonResponse를 사용하여 API 응답 생성
        CommonResponse<PointDto.GetPointsResponse> response = CommonResponse.of(pointsResponse);

        return ResponseEntity.ok(response);
    }

    // 회원별 적립금 적립 API
    @PostMapping("/api/v1/{memberId}/accrue")
    public ResponseEntity<CommonResponse<Void>> accruePoints(@RequestHeader(MEMBER_ID_HEADER) String memberName, @PathVariable Long memberId, @RequestParam @Min(1) int point) {
        accruePointService.accruePointsAsync(memberId, memberName, point);
        return ResponseEntity.ok(CommonResponse.of(ResponseCode.SUCCESS));
    }

    // 회원별 적립금 적립/사용 내역 조회 API (페이징 처리)
    @GetMapping("/api/v1/{memberId}/point-history")
    public ResponseEntity<CommonResponse<HistoryDto.Response>> getPagedPointHistoryByMemberId(
            @RequestHeader(MEMBER_ID_HEADER) String memberName,
            @PathVariable Long memberId,
            @PageableDefault Pageable pageable
            ) {
        final var userPointHistory = historyServiceImpl.getPagedUsageHistoryByMemberId(memberId, memberName,pageable);


        final var response = PointHistoryDtoMapper.of(userPointHistory);
        return ResponseEntity.ok(CommonResponse.of(response));
    }

    // 회원별 적립금 사용 API
    @PostMapping("/api/v1/{memberId}/use")
    public ResponseEntity<CommonResponse<PointDto.UsePointResponse>> usePoints(
            @RequestHeader(MEMBER_ID_HEADER) String memberName,
            @PathVariable Long memberId,
            @RequestParam int pointsToUse
    ) {
        CompletableFuture<PointDto.UsePointResponse> response = usePointService.usePointsAsync(memberId, memberName, pointsToUse);
        CommonResponse<PointDto.UsePointResponse> commonResponse = CommonResponse.of(response.join());

        return ResponseEntity.ok(commonResponse);
    }

    // 회원별 적립금 취소 API
    @PostMapping("/api/v1/{memberId}/cancel")
    public ResponseEntity<CommonResponse<PointDto.CancelPointResponse>> cancelPoints(@RequestHeader(MEMBER_ID_HEADER) String memberName, @PathVariable Long memberId, @RequestParam int pointsToCancel, int deductPointNo) {
        CompletableFuture<PointDto.CancelPointResponse> response = cancelPointService.cancelPointsAsync(memberId, memberName, pointsToCancel, deductPointNo);
        CommonResponse<PointDto.CancelPointResponse> commonResponse = CommonResponse.of(response.join());

        return ResponseEntity.ok(commonResponse);
    }

}
