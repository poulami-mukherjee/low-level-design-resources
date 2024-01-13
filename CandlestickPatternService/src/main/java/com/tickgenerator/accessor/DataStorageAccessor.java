package com.tickgenerator.accessor;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
@Getter
/**
 * Service class for accessing and managing data related to financial instruments, quotes, and candlesticks.
 */
public class DataStorageAccessor {
    private final InstrumentDataStorageAccessor instrumentDataStoreAccessor;
    private final QuotesDataStorageAccessor quotesDataStorageAccessor;
    private final CandlestickDataStorageAccessor candlestickDataStoreAccessor;
    public void cleanupForIsin(String isin) {
        instrumentDataStoreAccessor.cleanupInstrumentForId(isin);
        quotesDataStorageAccessor.cleanupQuoteForIsin(isin);
        candlestickDataStoreAccessor.cleanupCandlestickForIsin(isin);
    }
    public void cleanup() {
        instrumentDataStoreAccessor.cleanupInstrumentDataStore();
        quotesDataStorageAccessor.cleanupQuoteDataStore();
        candlestickDataStoreAccessor.cleanupCandlestickDataStore();
    }
}
