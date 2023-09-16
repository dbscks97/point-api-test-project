package com.marketboro.Premission;

import com.marketboro.Premission.entity.Member;
import com.marketboro.Premission.repository.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class MemberRepositoryTest {

    @Autowired
    private MemberRepository memberRepository;

    private Member member; // Declare Member object

    @BeforeEach
    public void setUp() {
        // Given
        member = Member.builder()
                .memberName("테스트회원1")
                .rewardPoints(10000)
                .build();
    }

    @Test
    public void 회원등록() {
        // When
        final Member result = memberRepository.save(member);

        // Then
        assertThat(result.getMemberId()).isNotNull();
        assertThat(result.getMemberName()).isEqualTo("테스트회원1");
        assertThat(result.getRewardPoints()).isEqualTo(10000);
    }

    @Test
    public void 회원이존재하는지테스트() {
        // When
        memberRepository.save(member);
        final List<Member> findResult = memberRepository.findByMemberName("테스트회원1");

        // Then
        assertThat(findResult).isNotEmpty();
        Member foundMember = findResult.get(0);
        assertThat(foundMember.getMemberId()).isNotNull();
        assertThat(foundMember.getMemberName()).isEqualTo("테스트회원1");
        assertThat(foundMember.getRewardPoints()).isEqualTo(10000);
    }
}
