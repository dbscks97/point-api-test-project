package com.marketboro.Premission.mapper;

import com.marketboro.Premission.dto.HistoryDto;
import com.marketboro.Premission.entity.History;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.stereotype.Service;


import java.util.List;
import java.util.stream.Collectors;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Service
public class PointHistoryDtoMapper {

    public static HistoryDto.Response of(final List<History> userPointHistory) {
        final var pointHistory = userPointHistory.stream()
                .map(PointHistoryDtoMapper::of)
                .collect(Collectors.toList());
        return HistoryDto.Response.of(pointHistory);
    }

    public static HistoryDto.PointHistory of(final History history) {
        return HistoryDto.PointHistory.of(
                history.getHistoryId(),
                history.getPoints(),
                history.getType(),
                history.getCreatedAt(),
                history.getUpdatedAt()
        );
    }
}
