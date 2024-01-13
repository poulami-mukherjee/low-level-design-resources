package com.tickgenerator.helper;

import com.tickgenerator.model.Candlestick;
import com.tickgenerator.model.Quote;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class CandlestickComputer {
    public Candlestick createInitialCandlestick(Quote quote) {
        Instant startOfMinute = truncateToMinute(quote.time());
        double price = quote.price();
        return new Candlestick(startOfMinute, price, price, price, price, startOfMinute.plus(59, ChronoUnit.SECONDS));
    }

    public Candlestick updateCandlestick(Candlestick existingCandlestick, Quote quote) {
        double highPrice = Math.max(existingCandlestick.highPrice(), quote.price());
        double lowPrice = Math.min(existingCandlestick.lowPrice(), quote.price());
        return new Candlestick(existingCandlestick.openTimestamp(), existingCandlestick.openPrice(), highPrice, lowPrice, quote.price(), existingCandlestick.closeTimestamp());
    }

    public Candlestick createMissingCandlestick(Candlestick lastCandlestick, Instant startOfMissingMinute) {
        return new Candlestick(startOfMissingMinute, lastCandlestick.openPrice(), lastCandlestick.highPrice(), lastCandlestick.lowPrice(), lastCandlestick.closePrice(), startOfMissingMinute.plus(59, ChronoUnit.SECONDS));
    }

    private Instant truncateToMinute(Instant time) {
        return time.truncatedTo(ChronoUnit.MINUTES);
    }
}
