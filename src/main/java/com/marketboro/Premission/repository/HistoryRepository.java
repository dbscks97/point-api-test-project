package com.marketboro.Premission.repository;

import com.marketboro.Premission.entity.History;
import com.marketboro.Premission.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface HistoryRepository extends JpaRepository<History, Long> {
    // 회원 별 포인트 적립/사용 내역 조회 (페이징 처리)
    Page<History> findByMemberMemberIdAndMemberMemberNameOrderByHistoryDateDesc(Long memberId, String memberName, Pageable pageable);

    // 해당 회원의 적립금 내역 중 포인트가 특정 값보다 큰 내역을 조회
    Page<History> findByMemberAndPointsGreaterThan(Member member, int points, Pageable pageable);

    // 해당 회원의 적립금 내역 중 포인트가 특정 값보다 작은 내역을 조회
    Page<History> findByMemberAndPointsLessThan(Member member, int points, Pageable pageable);
    List<History> findByMemberOrderByPriorityAscCreatedAtAsc(Member member);
    List<History> findByMember(Member member);

}


