package com.tickgenerator.model.datastores;

import com.tickgenerator.model.Quote;
import com.tickgenerator.model.QuoteEvent;
import org.springframework.stereotype.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListSet;

@Component
public class QuoteDataStore {
    private final ConcurrentHashMap<String, ConcurrentSkipListSet<Quote>> quotesDataStore = new ConcurrentHashMap<>();
    public void addInstrumentQuote(QuoteEvent quoteEvent) {
        String isin = quoteEvent.isin();
        Quote quote = quoteEvent.quote();
        quotesDataStore.computeIfAbsent(isin, k -> new ConcurrentSkipListSet<>(Comparator.comparing(Quote::time)))
                .add(quote);
    }
    public List<Quote> getQuotesForIsin(String isin) {
        ConcurrentSkipListSet<Quote> quotesSet = quotesDataStore.get(isin);
        if (quotesSet != null) {
            return new ArrayList<>(quotesSet);
        } else {
            return Collections.emptyList();
        }
    }
    public Boolean hasQuoteDataForIsin(String isin) {
        return quotesDataStore.containsKey(isin);
    }
    public void deleteInstrumentQuoteForIsin(String isin){
        quotesDataStore.remove(isin);
    }
    public void cleanupQuoteDataStore() {
        quotesDataStore.clear();
    }
}
