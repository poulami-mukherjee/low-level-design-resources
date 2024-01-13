package com.tickgenerator.model.datastores;

import com.tickgenerator.model.Instrument;
import com.tickgenerator.model.Status;
import org.springframework.stereotype.Component;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class InstrumentDataStore {
    private final ConcurrentHashMap<String, Status> instrumentDataStore = new ConcurrentHashMap<>();
    public void addInstrument(Instrument instrument) {
        String isin = instrument.isin();
        Status status = Status.ACTIVE;
        if(!isInstrumentPresent(isin)) {
            instrumentDataStore.putIfAbsent(isin, status);
        }
        instrumentDataStore.replace(isin, status);
    }
    public boolean isInstrumentPresent(String isin) {
        return instrumentDataStore.containsKey(isin);
    }
    public void deleteInstrumentForId(String isin) {
        Status status = Status.INACTIVE;
        instrumentDataStore.replace(isin, status);
    }
    public void cleanupInstrumentDataStore() {
        instrumentDataStore.clear();
    }
}
