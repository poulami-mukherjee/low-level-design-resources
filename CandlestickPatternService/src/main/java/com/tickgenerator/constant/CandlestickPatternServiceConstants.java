package com.tickgenerator.constant;
public final class CandlestickPatternServiceConstants {

    // Prevents instantiation
    private CandlestickPatternServiceConstants() {
        throw new AssertionError("Cannot instantiate a constant class");
    }

    // Websocket related constants
    public static final String BASE_URL = "ws://localhost:8032";
    public static final String INSTRUMENTS_ENDPOINT = "/instruments";
    public static final String QUOTES_ENDPOINT = "/quotes";

    // More constants can be added here...
}
