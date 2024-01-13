package com.tickgenerator.accessor;

import com.tickgenerator.handler.exception.PartnerServiceException;
import com.tickgenerator.model.Instrument;
import com.tickgenerator.model.datastores.InstrumentDataStore;
import com.tickgenerator.model.InstrumentEvent;
import com.tickgenerator.model.Type;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
@Component
@AllArgsConstructor
@Slf4j
public class InstrumentDataStorageAccessor {
    private final InstrumentDataStore instrumentDataStore;

    public void ingestInstrumentMessage(String payload) {
        try {
            InstrumentEvent instrumentEvent = parseInstrumentEvent(payload);
            persistInstrumentMessage(instrumentEvent);
        } catch (JSONException e) {
            log.error("Error Parsing Instrument Message Payload", e);
            throw e;
        } catch (PartnerServiceException e) {
            log.error("Unsupported Event Type", e);
            throw e;
        }
    }
    private InstrumentEvent parseInstrumentEvent(String payload) throws JSONException {
        JSONObject json = new JSONObject(payload);
        JSONObject data = json.getJSONObject("data");
        String typeString = json.getString("type");
        Type type = Type.valueOf(typeString.toUpperCase());
        String isin = data.getString("isin");
        String description = data.getString("description");
        Instrument instrument = Instrument.builder()
                .isin(isin)
                .description(description)
                .build();
        return new InstrumentEvent(instrument, type);
    }
    private void persistInstrumentMessage(InstrumentEvent event) {
        Type type = event.type();
        Instrument instrument = event.instrument();
        String isin = instrument.isin();
        if (type == Type.ADD) {
            instrumentDataStore.addInstrument(instrument);
        } else if (type == Type.DELETE) {
            cleanupInstrumentForId(isin);
        } else {
            throw new PartnerServiceException("Unsupported Event Type detected: " + type);
        }
    }
    public Boolean checkIfInstrumentExists(String isin) {
        return instrumentDataStore.isInstrumentPresent(isin);
    }
    public void cleanupInstrumentDataStore() {
        instrumentDataStore.cleanupInstrumentDataStore();
    }
    public void cleanupInstrumentForId(String isin){
        instrumentDataStore.deleteInstrumentForId(isin);
    }
}
