package com.tickgenerator.service;

import com.tickgenerator.accessor.DataStorageAccessor;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.client.WebSocketClient;
import jakarta.annotation.PostConstruct;
import com.tickgenerator.handler.WebSocketHandler;
import java.util.concurrent.ScheduledExecutorService;
import static com.tickgenerator.constant.CandlestickPatternServiceConstants.INSTRUMENTS_ENDPOINT;
import static com.tickgenerator.constant.CandlestickPatternServiceConstants.QUOTES_ENDPOINT;
import static com.tickgenerator.constant.CandlestickPatternServiceConstants.BASE_URL;

@Service
@AllArgsConstructor
@Slf4j
public class PartnerService {

    /*
    * @AllArgsConstructor generates a constructor with one parameter for each field in the class. T
    * It eliminates the need for explicit @Autowired or @Inject annotations on constructors
     */
    private final WebSocketClient webSocketClient;
    private final DataStorageAccessor dataStorageAccessor;
    private final ScheduledExecutorService executorService;

    @PostConstruct
    public void init() {
        connectToWebSocket(INSTRUMENTS_ENDPOINT);
        connectToWebSocket(QUOTES_ENDPOINT);
    }
    private void connectToWebSocket(String endpoint) {
        webSocketClient.doHandshake(new WebSocketHandler(
                endpoint,
                dataStorageAccessor,
                executorService
        ), BASE_URL + endpoint);
    }
}
