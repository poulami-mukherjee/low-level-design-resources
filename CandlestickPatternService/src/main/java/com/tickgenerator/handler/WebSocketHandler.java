package com.tickgenerator.handler;

import com.tickgenerator.accessor.InstrumentDataStorageAccessor;
import com.tickgenerator.accessor.QuotesDataStorageAccessor;
import com.tickgenerator.handler.exception.PartnerServiceException;
import lombok.extern.slf4j.Slf4j;
import lombok.AllArgsConstructor;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;
import com.tickgenerator.accessor.DataStorageAccessor;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import static com.tickgenerator.constant.CandlestickPatternServiceConstants.INSTRUMENTS_ENDPOINT;
import static com.tickgenerator.constant.CandlestickPatternServiceConstants.QUOTES_ENDPOINT;

@Slf4j
@AllArgsConstructor
public class WebSocketHandler implements org.springframework.web.socket.WebSocketHandler {
    private final String endpoint;
    private final DataStorageAccessor dataStorageAccessor;
    private final ScheduledExecutorService executorService;
    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        log.info("Connected to WebSocket endpoint: {}", endpoint);
        // Initial cleanup
        cleanupDataStores();
        // Schedule regular cleanup every 2 hours, starting 2 hours after the initial cleanup.
        executorService.scheduleAtFixedRate(this::cleanupDataStores, 2, 2, TimeUnit.HOURS);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) {
        if (!(message instanceof TextMessage)) {
            throw new PartnerServiceException("Unsupported message type: {}" + message.getClass());
        }
        TextMessage textMessage = (TextMessage) message;
        String payload = textMessage.getPayload();
        log.info("Received message on {}: {}", endpoint, payload);
        if (endpoint.equals(INSTRUMENTS_ENDPOINT)) {
            InstrumentDataStorageAccessor accessor = dataStorageAccessor.getInstrumentDataStoreAccessor();
            accessor.ingestInstrumentMessage(payload);
        } else if (endpoint.equals(QUOTES_ENDPOINT)) {
            QuotesDataStorageAccessor accessor = dataStorageAccessor.getQuotesDataStorageAccessor();
            accessor.ingestQuoteMessage(payload);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        log.error("Transport error in WebSocket connection: {}", endpoint, exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        log.info("Closed WebSocket connection: {}, CloseStatus: {}", endpoint, status);
    }
    @Override
    public boolean supportsPartialMessages() {
        return false;
    }
    private void cleanupDataStores() {
        dataStorageAccessor.cleanup();
    }
}
