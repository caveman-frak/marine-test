package uk.co.bluegecko.marine.test.jassert;

import lombok.experimental.UtilityClass;
import org.assertj.core.api.Assertions;
import org.assertj.core.api.Condition;

import java.math.BigDecimal;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;

@UtilityClass
public class Conditions {

	/**
	 * Test for numbers being equal within a threshold.
	 * Allows sensible {@link Object#equals(Object)} checks on floats and doubles.
	 *
	 * @param threshold the margin for error on the equality check.
	 * @param <T>       must extend {@link Number}.
	 * @return a {@link BiPredicate} for testing numeric equality.
	 */
	public <T extends Number> BiPredicate<T, T> isEqualTo(final double threshold) {
		return (a, e) -> Math.abs(a.doubleValue() - e.doubleValue()) < threshold;
	}

	/**
	 * Test for BigDecimals being equal,
	 * uses {@link Comparable#compareTo} are rather than {@link Object#equals}.
	 *
	 * @param <T> a {@link BigDecimal}.
	 * @return a {@link BiPredicate} for testing numeric equality.
	 */
	public <T extends BigDecimal> BiPredicate<T, T> isEqualTo() {
		return (a, e) -> a.compareTo(e) == 0;
	}
	
	/**
	 * Build a described {@link Condition} using the predicate.
	 *
	 * @param predicate   the predicate to test.
	 * @param description short description of the test.
	 * @param expected    the expected value to test against.
	 * @param <T>         the type of the value being tested.
	 * @return a condition to be used with {@link Assertions#assertThat}.
	 */
	public <T> Condition<T> condition(final BiPredicate<T, T> predicate, final String description,
	                                  final T expected) {
		return new Condition<>(a -> predicate.test(a, expected),
				"%s \"%s\"", description, expected);
	}

	/**
	 * Build a described {@link Condition} using the function and predicate.
	 *
	 * @param function    extract a value for testing.
	 * @param extract     short description of the extracted value.
	 * @param description short description of the test.
	 * @param expected    the expected value to test against.
	 * @param <T>         the type of the value being tested.
	 * @param <U>         the type of the value being extracted.
	 * @return a condition to be used with {@link Assertions#assertThat}.
	 */
	public <T, U> Condition<T> extract(final Function<T, U> function, final String extract,
	                                   final BiPredicate<U, U> predicate, final String description,
	                                   final U expected) {
		return new Condition<>(a -> predicate.test(function.apply(a), expected),
				"%s %s \"%s\"", extract, description, expected);
	}

	/**
	 * Build a described {@link Condition} using the function and predicate.
	 *
	 * @param function    extract a value for testing.
	 * @param description short description of the extracted value and the test.
	 * @param expected    the expected value to test against.
	 * @param <T>         the type of the value being tested.
	 * @param <U>         the type of the value being extracted.
	 * @return a condition to be used with {@link Assertions#assertThat}.
	 */
	public <T, U> Condition<T> extract(final Function<T, U> function,
	                                   final BiPredicate<U, U> predicate, final String description,
	                                   final U expected) {
		return new Condition<>(a -> predicate.test(function.apply(a), expected),
				"%s \"%s\"", description, expected);
	}

	/**
	 * Build a described {@link Condition} using the function and predicate.
	 *
	 * @param function    extract a value for testing.
	 * @param description short description of the extracted value and the test.
	 * @param <T>         the type of the value being tested.
	 * @param <U>         the type of the value being extracted.
	 * @return a condition to be used with {@link Assertions#assertThat}.
	 */
	public <T, U> Condition<T> extract(final Function<T, U> function,
	                                   final Predicate<U> predicate, final String description
	) {
		return new Condition<>(a -> predicate.test(function.apply(a)),
				"%s", description);
	}

}
