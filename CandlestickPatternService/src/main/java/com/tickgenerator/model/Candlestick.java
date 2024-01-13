package com.tickgenerator.model;

import lombok.Builder;
import java.time.Instant;

@Builder
public record Candlestick(
        Instant openTimestamp,
        Double openPrice,
        Double highPrice,
        Double lowPrice,
        Double closePrice,
        Instant closeTimestamp
) implements Comparable<Candlestick> {

    @Override
    public int compareTo(Candlestick other) {
        // Sorting Candlestick objects and arrange them in order where the Candlestick with the earliest closeTimestamp comes first, followed by the next earliest, and so on.
        return this.closeTimestamp.compareTo(other.closeTimestamp);
    }
}
