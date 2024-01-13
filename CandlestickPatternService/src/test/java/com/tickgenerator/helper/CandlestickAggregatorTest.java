package com.tickgenerator.helper;

import com.tickgenerator.accessor.CandlestickDataStorageAccessor;
import com.tickgenerator.accessor.DataStorageAccessor;
import com.tickgenerator.accessor.QuotesDataStorageAccessor;
import com.tickgenerator.model.Candlestick;
import com.tickgenerator.model.Candlesticks;
import com.tickgenerator.model.Quote;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.time.Instant;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CandleStickAggregatorTest {

    @Mock
    private DataStorageAccessor dataStorageAccessor;

    @Mock
    private CandlestickComputer candlestickComputer;

    @Mock
    private CandlestickDataStorageAccessor candlestickDataStorageAccessor;

    @Mock
    private QuotesDataStorageAccessor quotesDataStorageAccessor;

    @InjectMocks
    private CandleStickAggregator candleStickAggregator;

    private final String isin = "testIsin";
    private final Instant startTime = Instant.parse("2024-01-05T00:00:00Z");
    private final Instant endTime = Instant.parse("2024-01-05T01:00:00Z");

    @BeforeEach
    void setUp() {
        when(dataStorageAccessor.getCandlestickDataStoreAccessor()).thenReturn(candlestickDataStorageAccessor);
        when(dataStorageAccessor.getQuotesDataStorageAccessor()).thenReturn(quotesDataStorageAccessor);
    }

    @Test
    void shouldReturnEmptyCandlesticksWhenNoDataExists() {
        when(candlestickDataStorageAccessor.getStoredCandlestickDataForIsin(isin)).thenReturn(new Candlesticks(new ConcurrentSkipListSet<>()));
        when(quotesDataStorageAccessor.checkIfQuoteDataExistsForIsin(isin)).thenReturn(false);

        Candlesticks result = candleStickAggregator.generateCandleSticksForIsin(isin, startTime, endTime);

        assertTrue(result.candlesticks().isEmpty());
    }



    // Additional tests can be added to cover more scenarios like updating existing candlesticks, handling missing candlesticks, etc.
}
