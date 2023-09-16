package com.marketboro.Premission.entity;


import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "histories")
public class History {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long historyId;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;
    private int points;

    @Temporal(TemporalType.TIMESTAMP)
    private Date historyDate;
}
