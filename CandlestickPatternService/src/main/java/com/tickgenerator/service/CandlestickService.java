package com.tickgenerator.service;

import com.tickgenerator.helper.CandleStickAggregator;
import com.tickgenerator.model.Candlesticks;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

/*
This service should handle the logic for determining the time range (last 30 minutes) and invoking the aggregator.
 */
@Service
@AllArgsConstructor
@Slf4j
public class CandlestickService {
    CandleStickAggregator aggregator;
    public Candlesticks getCandlesticks(String isin) {
        // Current time as the end time
        Instant endTime = Instant.now();
        // Calculate 30 minutes before the current time
        Instant startTime = endTime.minus(30, ChronoUnit.MINUTES);
        return aggregator.generateCandleSticksForIsin(isin, startTime, endTime);
    }
}
