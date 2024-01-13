package com.tickgenerator.accessor;

import com.tickgenerator.model.datastores.CandlestickDataStore;
import com.tickgenerator.model.Candlesticks;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class CandlestickDataStorageAccessor {
    private final CandlestickDataStore candlestickDataStore;
    public void addCandlesticks(String isin, Candlesticks candlesticks) {
        candlestickDataStore.addCandlesticks(isin, candlesticks);
    }
    public Candlesticks getStoredCandlestickDataForIsin(String isin) {
        return candlestickDataStore.getStoredCandlestickDataForIsin(isin);
    }
    public Boolean checkIfCandlesticksDataExistsForIsin(String isin) { return candlestickDataStore.hasCandlestickDataForIsin(isin); }
    public void cleanupCandlestickDataStore() {
        candlestickDataStore.cleanupCandlesticksDataStore();
    }
    public void cleanupCandlestickForIsin(String isin) {
        candlestickDataStore.deleteCandlestickForIsin(isin);
    }
}
