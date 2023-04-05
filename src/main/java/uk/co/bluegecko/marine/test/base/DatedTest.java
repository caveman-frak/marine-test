package uk.co.bluegecko.marine.test.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.ZoneOffset;

public abstract class DatedTest {

	private final Clock clock;
	private final ObjectMapper objectMapper;

	protected DatedTest() {
		clock = Clock.fixed(
				LocalDateTime.of(2000, Month.JUNE, 15, 12, 30).toInstant(ZoneOffset.UTC),
				ZoneOffset.UTC);
		objectMapper = JsonMapper.builder()
				.defaultLeniency(true)
				.addModule(new JavaTimeModule())
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
				.build();
	}

	protected final ObjectMapper objectMapper() {
		return objectMapper;
	}

	protected final Clock clock() {
		return clock;
	}
}