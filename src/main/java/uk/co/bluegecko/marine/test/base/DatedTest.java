package uk.co.bluegecko.marine.test.base;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter(AccessLevel.PROTECTED)
@Accessors(fluent = true, makeFinal = true)
public abstract class DatedTest extends BaseTest implements DateTimeFixture {

	LocalDate date;
	LocalTime time;
	ZoneOffset zone;
	Clock clock;

	protected DatedTest() {
		date = LocalDate.of(YEAR, MONTH, DAY);
		time = LocalTime.of(HOUR, MINUTE, SECOND);
		zone = ZONE;
		clock = Clock.fixed(
				LocalDateTime.of(date, time).toInstant(zone),
				zone);
	}

}