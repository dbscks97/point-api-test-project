package com.marketboro.Premission.repository;

import com.marketboro.Premission.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {
    Member findByMemberId(Long memberId);
    List<Member> findByMemberName(String memberName);
}
