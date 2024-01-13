package com.tickgenerator;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@Slf4j
public class CandlestickPatternGeneratorApplication {
	public static void main(String[] args) {
		log.info("CandlestickPatternGenerator Application Starting up");
		SpringApplication.run(CandlestickPatternGeneratorApplication.class, args);
	}
}
