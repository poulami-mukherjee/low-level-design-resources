package com.tickgenerator.model;

import lombok.Builder;
@Builder
public record QuoteEvent(
        String isin,
        Quote quote
) {}
