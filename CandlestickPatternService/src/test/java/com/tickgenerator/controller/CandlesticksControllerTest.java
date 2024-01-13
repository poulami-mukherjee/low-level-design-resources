package com.tickgenerator.controller;

import com.tickgenerator.accessor.DataStorageAccessor;
import com.tickgenerator.accessor.InstrumentDataStorageAccessor;
import com.tickgenerator.model.Candlestick;
import com.tickgenerator.model.Candlesticks;
import com.tickgenerator.model.datatransferobject.CandlestickDto;
import com.tickgenerator.service.CandlestickService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CandlesticksControllerTest {

    @Mock
    private DataStorageAccessor dataStorageAccessor;

    @Mock
    private InstrumentDataStorageAccessor instrumentDataStorageAccessor;

    @Mock
    private CandlestickService candlestickService;

    @InjectMocks
    private CandlesticksController controller;

    @BeforeEach
    void setUp() {
        when(dataStorageAccessor.getInstrumentDataStoreAccessor()).thenReturn(instrumentDataStorageAccessor);
    }

    @Test
    void getCandlesticks_WhenInstrumentExistsAndHasData() {
        String isin = "testIsin";
        Candlestick candlestick = new Candlestick(Instant.now(), 100.0, 105.0, 95.0, 102.0, Instant.now());
        Candlesticks candlesticks = new Candlesticks(new ConcurrentSkipListSet<>(Collections.singletonList(candlestick)));

        when(instrumentDataStorageAccessor.checkIfInstrumentExists(isin)).thenReturn(true);
        when(candlestickService.getCandlesticks(isin)).thenReturn(candlesticks);

        ResponseEntity<List<CandlestickDto>> response = controller.getCandlesticks(isin);

        assertEquals(200, response.getStatusCodeValue());
        assertFalse(response.getBody().isEmpty());
        CandlestickDto dto = response.getBody().get(0);
        assertNotNull(dto);
        validateCandlestickDto(dto, candlestick);
    }

    @Test
    void getCandlesticks_WhenInstrumentDoesNotExist() {
        String isin = "testIsin";
        when(instrumentDataStorageAccessor.checkIfInstrumentExists(isin)).thenReturn(false);

        ResponseEntity<List<CandlestickDto>> response = controller.getCandlesticks(isin);

        assertEquals(204, response.getStatusCodeValue());
        assertNull(response.getBody());
    }

    @Test
    void getCandlesticks_WhenInternalErrorOccurs() {
        String isin = "testIsin";
        when(instrumentDataStorageAccessor.checkIfInstrumentExists(isin)).thenThrow(new RuntimeException("Internal Error"));

        Exception exception = assertThrows(RuntimeException.class, () -> controller.getCandlesticks(isin));
        assertEquals("Internal Error", exception.getMessage());
    }

    private void validateCandlestickDto(CandlestickDto dto, Candlestick candlestick) {
        // Add validations to check if the DTO fields match the expected values
        assertEquals(candlestick.openPrice(), dto.openPrice());
        assertEquals(candlestick.highPrice(), dto.highPrice());
        assertEquals(candlestick.lowPrice(), dto.lowPrice());
        assertEquals(candlestick.closePrice(), dto.closePrice());
    }
}
