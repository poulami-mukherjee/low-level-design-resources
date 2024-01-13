package com.tickgenerator.accessor;

import com.tickgenerator.model.Quote;
import com.tickgenerator.model.datastores.QuoteDataStore;
import com.tickgenerator.model.QuoteEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Component;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@AllArgsConstructor
@Slf4j
public class QuotesDataStorageAccessor {
    private final QuoteDataStore quoteDataStore;
    public void ingestQuoteMessage(String payload) {
        try {
            QuoteEvent quoteEvent = parseQuoteEvent(payload);
            persistQuoteEvent(quoteEvent);
        } catch (JSONException e) {
            log.error("Error parsing JSON message", e);
        }
    }
    private QuoteEvent parseQuoteEvent(String payload) throws JSONException {
        JSONObject json = new JSONObject(payload);
        JSONObject data = json.getJSONObject("data");
        double price = data.getDouble("price");
        String isin = data.getString("isin");
        Instant time = Instant.now();

        Quote quote = Quote.builder()
                .price(price)
                .time(time)
                .build();

        return new QuoteEvent(isin, quote);
    }
    private void persistQuoteEvent(QuoteEvent event) {
        quoteDataStore.addInstrumentQuote(event);
    }
    public List<Quote> getQuotesForIsin(String isin) {
        return quoteDataStore.getQuotesForIsin(isin);
    }

    public Optional<Instant> getLatestQuoteTimestampForIsin(String isin) {
        List<Quote> quotesList = quoteDataStore.getQuotesForIsin(isin);
        if (!quotesList.isEmpty()) {
            // Retrieve the time of the last quote in the list
            return Optional.of(quotesList.get(quotesList.size() - 1).time());
        } else {
            // Return an empty Optional if there are no quotes
            return Optional.empty();
        }
    }


    public Boolean checkIfQuoteDataExistsForIsin(String isin) { return quoteDataStore.hasQuoteDataForIsin(isin); }
    public void cleanupQuoteDataStore() {
        quoteDataStore.cleanupQuoteDataStore();
    }
    public void cleanupQuoteForIsin(String isin) {
        quoteDataStore.deleteInstrumentQuoteForIsin(isin);
    }
}
