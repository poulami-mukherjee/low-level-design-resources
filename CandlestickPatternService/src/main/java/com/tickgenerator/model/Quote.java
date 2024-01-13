package com.tickgenerator.model;

import lombok.Builder;
import java.time.Instant;
@Builder
public record Quote(
        Instant time,
        Double price
) {}
