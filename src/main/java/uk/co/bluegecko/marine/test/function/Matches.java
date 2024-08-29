package uk.co.bluegecko.marine.test.function;

import java.util.function.Predicate;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.hamcrest.Matcher;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class Matches<T> implements Predicate<T> {

	Matcher<T> matcher;

	@Override
	public boolean test(T actual) {
		return matcher.matches(actual);
	}

	public static <T> Predicate<T> matches(Matcher<T> matcher) {
		return new Matches<>(matcher);
	}

}