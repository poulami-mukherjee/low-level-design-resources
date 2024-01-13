package com.tickgenerator.model.datatransferobject;

import lombok.Builder;
@Builder
public record CandlestickDto(
        String openTimestamp,
        Double openPrice,
        Double highPrice,
        Double lowPrice,
        Double closePrice,
        String closeTimestamp
) {}