package com.tickgenerator.model;

import lombok.Builder;
@Builder
public record Instrument(
        String description,
        String isin
) {}
