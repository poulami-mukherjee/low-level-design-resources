package com.tickgenerator.model.datastores;

import com.tickgenerator.model.Candlesticks;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Component
public class CandlestickDataStore {
    private final ConcurrentHashMap<String, Candlesticks> candlestickDataStore = new ConcurrentHashMap<>();
    public void cleanupCandlesticksDataStore() {
        candlestickDataStore.clear();
    }
    public Boolean hasCandlestickDataForIsin(String isin) {
        return candlestickDataStore.containsKey(isin);
    }
    public void addCandlesticks(String isin, Candlesticks newCandlesticks) {
        candlestickDataStore.compute(isin, (key, existingCandlesticks) -> {
            if (!hasCandlestickDataForIsin(isin)) {
                // If there is no existing entry for this ISIN, create a new one
                return newCandlesticks;
            } else {
                // If there is an existing entry, add all new candlesticks to it
                existingCandlesticks.candlesticks().addAll(newCandlesticks.candlesticks());
                return existingCandlesticks;
            }
        });
    }

    public Candlesticks getStoredCandlestickDataForIsin(String isin) {
        Candlesticks candlesticks = candlestickDataStore.get(isin);
        if (candlesticks!= null && !candlesticks.isEmpty()) {
            return candlestickDataStore.get(isin);
        } else {
            return new Candlesticks(new ConcurrentSkipListSet<>());
        }
    }
    public void deleteCandlestickForIsin(String isin){
        candlestickDataStore.remove(isin);
    }
}
