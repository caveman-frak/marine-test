package uk.co.bluegecko.marine.test.jassert;

import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static uk.co.bluegecko.marine.test.jassert.Conditions.condition;
import static uk.co.bluegecko.marine.test.jassert.Conditions.isEqualTo;

class ConditionsTest {

	@Test
	void testIsEqualToNumber() {
		assertThat(Conditions.isEqualTo(0.000005).test(1.000000, 1.000001))
				.as("1.000000 == 1.000001").isTrue();
		assertThat(Conditions.isEqualTo(0.000005).test(1.000000, 0.999999))
				.as("1.000000 == 0.999999").isTrue();
		assertThat(Conditions.isEqualTo(0.000005).test(1.000000, 1.000010))
				.as("1.000000 != 1.000010").isFalse();
		assertThat(Conditions.isEqualTo(0.000005).test(1.000000, 0.999990))
				.as("1.000000 != 0.999990").isFalse();
	}

	@Test
	void testIsEqualToBigDecimal() {
		BigDecimal actual = BigDecimal.valueOf(1, -2);
		BigDecimal expected = BigDecimal.valueOf(100);
		assertThat(actual.equals(expected))
				.as("Basic equality fails").isFalse();
		assertThat(isEqualTo().test(actual, expected))
				.as("Comparative equality passes").isTrue();
	}

	@Test
	void testCondition() {
		Condition<String> fairyTale = condition(String::startsWith, "starting with", "Once");
		assertThat("Once upon a time")
				.as("Is a fairy tale")
				.is(fairyTale);
		assertThat("Life is grim")
				.as("Is not a fairy tale")
				.isNot(fairyTale);
		assertThatExceptionOfType(AssertionError.class).isThrownBy(
						() -> assertThat("Life is grim").is(fairyTale))
				.withMessage("\nExpecting actual:\n  \"Life is grim\"\nto be starting with \"Once\"");
	}

	@Test
	void testExtract() {
		Condition<LocalDate> isJanuary = Conditions.extracted(LocalDate::getMonth, "having month",
				Object::equals, "equal to", Month.JANUARY);
		assertThat(LocalDate.of(2020, Month.JANUARY, 1))
				.as("Start of year")
				.is(isJanuary);
		assertThat(LocalDate.of(2020, Month.DECEMBER, 31))
				.as("End of year")
				.isNot(isJanuary);
		assertThatExceptionOfType(AssertionError.class).isThrownBy(
						() -> assertThat(LocalDate.of(2020, Month.DECEMBER, 31))
								.is(isJanuary))
				.withMessage("""

						Expecting actual:
						  2020-12-31 (java.time.LocalDate)
						to be having month equal to "JANUARY\"""");
	}

	@Test
	void testExtractWithoutExtractText() {
		Condition<LocalDate> isJanuary = Conditions.extracted(LocalDate::getMonth,
				Object::equals, "having month equal to", Month.JANUARY);
		assertThat(LocalDate.of(2020, Month.JANUARY, 1))
				.as("Start of year")
				.is(isJanuary);
		assertThat(LocalDate.of(2020, Month.DECEMBER, 31))
				.as("End of year")
				.isNot(isJanuary);
		assertThatExceptionOfType(AssertionError.class).isThrownBy(
						() -> assertThat(LocalDate.of(2020, Month.DECEMBER, 31))
								.is(isJanuary))
				.withMessage("""

						Expecting actual:
						  2020-12-31 (java.time.LocalDate)
						to be having month equal to "JANUARY\"""");
	}

	@Test
	void testExtractWithDescription() {
		Condition<LocalDate> isJanuary = Conditions.extracted(LocalDate::getMonth, "having month",
				Object::equals, "equal to", Month.JANUARY);
		assertThatExceptionOfType(AssertionError.class).isThrownBy(
						() -> assertThat(LocalDate.of(2020, Month.DECEMBER, 31))
								.as("End of Year")
								.is(isJanuary))
				.withMessage("""
						[End of Year]\s
						Expecting actual:
						  2020-12-31 (java.time.LocalDate)
						to be having month equal to "JANUARY\"""");
	}

}