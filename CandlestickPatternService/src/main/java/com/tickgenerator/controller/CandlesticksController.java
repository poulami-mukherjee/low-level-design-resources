package com.tickgenerator.controller;

import com.tickgenerator.accessor.DataStorageAccessor;
import com.tickgenerator.accessor.InstrumentDataStorageAccessor;
import com.tickgenerator.helper.DateTimeFormatterHelper;
import com.tickgenerator.model.Candlestick;
import com.tickgenerator.model.Candlesticks;
import com.tickgenerator.model.datatransferobject.CandlestickDto;
import com.tickgenerator.service.CandlestickService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@AllArgsConstructor
public class
CandlesticksController {
    private final DataStorageAccessor dataStorageAccessor;
    private final CandlestickService candlestickService;

    /**
     * Handles the GET request to retrieve candlestick data for a specified financial instrument.
     *
     * @param isin The International Securities Identification Number (ISIN) of the financial instrument.
     * @return ResponseEntity containing the Candlesticks data.
     *         Returns a 200 OK response with the candlesticks data if the instrument exists and has data.
     *         Returns a 204 No Content response if the instrument does not exist or has no candlestick data.
     *         400 Error
     *         In case of an internal server error, logs the error and throws an exception.
     */
    @GetMapping("/candlesticks")
    public ResponseEntity<List<CandlestickDto>> getCandlesticks(@RequestParam(name = "isin", required = true) String isin) {
        try {
            InstrumentDataStorageAccessor accessor = dataStorageAccessor.getInstrumentDataStoreAccessor();
            Boolean isPresent = accessor.checkIfInstrumentExists(isin);
            if (!isPresent) {
                return ResponseEntity.noContent().build();
            }
            Candlesticks candlesticks = candlestickService.getCandlesticks(isin);
            if (!candlesticks.isEmpty()) {
                List<CandlestickDto> candlestickDTOs = candlesticks.candlesticks().stream()
                        .map(this::convertToDTO)
                        .collect(Collectors.toList());
                return ResponseEntity.ok(candlestickDTOs);
            } else {
                return ResponseEntity.noContent().build();
            }
        } catch (Exception exception) {
            log.error("Internal Server Error", exception);
            throw exception;
        }
    }
    private CandlestickDto convertToDTO(Candlestick candlestick) {
        CandlestickDto dto = CandlestickDto.builder()
                .openTimestamp(DateTimeFormatterHelper.formatInstant(candlestick.openTimestamp()))
                .openPrice(candlestick.openPrice())
                .highPrice(candlestick.highPrice())
                .lowPrice(candlestick.lowPrice())
                .closePrice(candlestick.closePrice())
                .closeTimestamp(DateTimeFormatterHelper.formatInstant(candlestick.closeTimestamp()))
                .build();
        return dto;
    }
}

