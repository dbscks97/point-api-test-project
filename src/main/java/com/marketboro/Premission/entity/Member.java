package com.marketboro.Premission.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;


import java.util.List;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "members")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;

    @Column(nullable = false, length = 20)
    private String memberName;

    @Column(nullable = false)
    @ColumnDefault("0")
    private int rewardPoints;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<History> histories;
}