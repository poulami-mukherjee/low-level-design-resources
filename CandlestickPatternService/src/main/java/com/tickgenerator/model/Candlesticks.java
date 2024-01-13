package com.tickgenerator.model;

import lombok.Builder;
import java.util.concurrent.ConcurrentSkipListSet;
@Builder
public record Candlesticks(ConcurrentSkipListSet<Candlestick> candlesticks) {
    public Boolean isEmpty() {
        return candlesticks().isEmpty();
    }
}
