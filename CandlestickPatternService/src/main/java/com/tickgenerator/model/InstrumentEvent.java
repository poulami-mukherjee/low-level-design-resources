package com.tickgenerator.model;

import lombok.Builder;
@Builder
public record InstrumentEvent(
        Instrument instrument,
        Type type
) {}