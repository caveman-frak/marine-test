package uk.co.bluegecko.marine.test.configuration;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.Month;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.random.RandomGenerator;
import lombok.NonNull;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import uk.co.bluegecko.marine.shared.configuration.SharedConfiguration;
import uk.co.bluegecko.marine.test.random.SteppingGenerator;

@TestConfiguration
public class TestApplicationConfiguration extends SharedConfiguration {

	@SuppressWarnings("SameReturnValue")
	@Bean
	public ZoneId zone() {
		return ZoneOffset.UTC;
	}

	@Bean
	public LocalDate date() {
		return LocalDate.of(2000, Month.JUNE, 15);
	}

	@Bean
	public LocalTime time() {
		return LocalTime.of(12, 30, 10);
	}

	@Bean
	public LocalDateTime dateTime(@NonNull LocalDate date, @NonNull LocalTime time) {
		return LocalDateTime.of(date, time);
	}

	@Bean
	public ZonedDateTime zonedDateTime(@NonNull LocalDateTime dateTime, @NonNull ZoneId zone) {
		return ZonedDateTime.of(dateTime, zone);
	}

	@Bean
	public Instant instant(@NonNull ZonedDateTime dateTime) {
		return dateTime.toInstant();
	}

	@Bean
	@Primary
	public Clock clock(@NonNull Instant instant, @NonNull ZoneId zone) {
		return Clock.fixed(instant, zone);
	}

	@Override
	@Bean
	@Primary
	public RandomGenerator randomGenerator() {
		return new SteppingGenerator();
	}

}