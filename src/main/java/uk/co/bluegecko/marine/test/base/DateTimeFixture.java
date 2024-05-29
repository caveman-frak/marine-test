package uk.co.bluegecko.marine.test.base;

import java.time.Month;
import java.time.ZoneOffset;

public interface DateTimeFixture {

	ZoneOffset ZONE = ZoneOffset.UTC;
	int YEAR = 2000;
	Month MONTH = Month.JUNE;
	int DAY = 15;
	int HOUR = 12;
	int MINUTE = 30;
	int SECOND = 10;

}