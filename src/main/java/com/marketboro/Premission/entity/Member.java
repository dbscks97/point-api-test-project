package com.marketboro.Premission.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "members")
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long memberId;
    private int rewardPoints;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL)
    private List<History> histories;
}
