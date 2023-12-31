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
    List<History> findByMemberMemberIdOrderByHistoryDateDesc(Long memberId, Pageable pageable);


    List<History> findByMember(Member member);
    List<History> findByMemberAndTypeAndDeductPointNo(Member member, String type, int deductPointNo);

}


