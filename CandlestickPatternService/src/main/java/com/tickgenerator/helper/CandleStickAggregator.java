package com.tickgenerator.helper;

import com.tickgenerator.accessor.CandlestickDataStorageAccessor;
import com.tickgenerator.accessor.DataStorageAccessor;
import com.tickgenerator.accessor.QuotesDataStorageAccessor;
import com.tickgenerator.model.Candlestick;
import com.tickgenerator.model.Candlesticks;
import com.tickgenerator.model.Quote;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.stream.Collectors;

/*
This class should be responsible for fetching the necessary data (quotes and existing candlesticks) and computing the new candlesticks.
 */
@Slf4j
@Service
@AllArgsConstructor
public class CandleStickAggregator {
    private final DataStorageAccessor dataStorageAccessor;
    private final CandlestickComputer candlestickComputer;

    public Candlesticks generateCandleSticksForIsin(String isin, Instant startTime, Instant endTime) {
        CandlestickDataStorageAccessor candlestickAccessor = dataStorageAccessor.getCandlestickDataStoreAccessor();

        Candlesticks existingCandlesticks = candlestickAccessor.getStoredCandlestickDataForIsin(isin);

        QuotesDataStorageAccessor quoteAccessor = dataStorageAccessor.getQuotesDataStorageAccessor();

        // Logic to check if both Candlestick Datastore and Quote Datastore is Not Empty
        Boolean hasCandlestickData = !existingCandlesticks.isEmpty();
        Boolean hasQuoteData = quoteAccessor.checkIfQuoteDataExistsForIsin(isin);

        // If Both are Empty for the given isin we return and Empty List in the Candlestick Object
        if (!hasCandlestickData && !hasQuoteData) {
            return new Candlesticks(new ConcurrentSkipListSet<>());
        }

        // Determine the last candlestick and the latest quote time, if available
        Optional<Candlestick> lastCandlestick = hasCandlestickData ?
                Optional.of(existingCandlesticks.candlesticks().last()) : Optional.empty();
        Optional<Instant> latestQuoteTime = quoteAccessor.getLatestQuoteTimestampForIsin(isin);

        // If the latest quote is not after the last candlestick, return existing candlesticks
        if (latestQuoteTime.isPresent() && lastCandlestick.isPresent()
                && !latestQuoteTime.get().isAfter(lastCandlestick.get().closeTimestamp())) {
            return existingCandlesticks; // modify according to timestamp
        }

        // Fetch and filter quotes for the given ISIN and time range.
        List<Quote> quotes = dataStorageAccessor.getQuotesDataStorageAccessor().getQuotesForIsin(isin).stream()
                .filter(quote -> !quote.time().isBefore(startTime) && quote.time().isBefore(endTime))
                .collect(Collectors.toList());

        // Prepare a set to hold the new candlesticks

        // Navigable set - get only the part of set we are interested

        ConcurrentSkipListSet<Candlestick> candlesticks = new ConcurrentSkipListSet<>(existingCandlesticks.candlesticks());

        /*
         Process the quotes to generate candlesticks
         The candlesticks set is passed by value but points to same Object.
         This means that any modifications made to this set within the processQuotes method (like adding new Candlestick objects) will be reflected in the candlesticks set in the generateCandleSticksForIsin method.
         */
        processQuotes(quotes, candlesticks, lastCandlestick.orElse(null), startTime, endTime);

        // Update the data store with the new candlesticks
        dataStorageAccessor.getCandlestickDataStoreAccessor().addCandlesticks(isin, new Candlesticks(candlesticks));

        return new Candlesticks(candlesticks);
    }

    // Processes the list of quotes and updates/creates candlesticks accordingly
    private void processQuotes(List<Quote> quotes, ConcurrentSkipListSet<Candlestick> candlesticks, Candlestick lastCandlestick, Instant startTime, Instant endTime) {
        Instant currentMinute = lastCandlestick == null ? startTime.truncatedTo(ChronoUnit.MINUTES) : lastCandlestick.closeTimestamp().plus(1, ChronoUnit.MINUTES);
        Candlestick currentCandlestick = lastCandlestick;

        for (Quote quote : quotes) {
            Instant quoteMinute = truncateToMinute(quote.time());
            if (currentCandlestick == null || !quoteMinute.equals(currentMinute)) {
                // New minute, create a new candlestick
                currentMinute = quoteMinute;
                currentCandlestick = candlestickComputer.createInitialCandlestick(quote);
                candlesticks.add(currentCandlestick);
            } else {
                // Same minute, update the existing candlestick
                currentCandlestick = candlestickComputer.updateCandlestick(currentCandlestick, quote);
            }
        }
        fillMissingCandlesticks(candlesticks, currentCandlestick, currentMinute, endTime);
    }





    // Fills in missing candlesticks between two points in time
    private void fillMissingCandlesticks(ConcurrentSkipListSet<Candlestick> candlesticks, Candlestick lastCandlestick, Instant start, Instant end) {
        if (lastCandlestick == null) {
            return; // Do not create missing candlesticks if there are no existing ones
        }
        Instant current = start;
        while (current.isBefore(end)) {
            Candlestick missingCandlestick = candlestickComputer.createMissingCandlestick(lastCandlestick, current);
            candlesticks.add(missingCandlestick);
            current = current.plus(1, ChronoUnit.MINUTES);
            lastCandlestick = missingCandlestick;
        }
    }

    // Utility method to truncate an Instant to the start of its minute
    private Instant truncateToMinute(Instant time) {
        return time.truncatedTo(ChronoUnit.MINUTES);
    }
}
