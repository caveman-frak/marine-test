package uk.co.bluegecko.marine.test.data;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestFactory;

import java.util.UUID;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class GeneratorsTest {

	@Test
	void testUuid() {
		assertThat(Generators.uuid().get()).isEqualTo(new UUID(0, 0));
	}

	@Test
	void testUuidWithInitial() {
		assertThat(Generators.uuid(1, 10).get()).isEqualTo(new UUID(1, 10));
	}

	@Test
	void testNumber() {
		assertThat(Generators.number().get()).isEqualTo(0);
	}

	@Test
	void testNumberWithInitial() {
		assertThat(Generators.number(10).get()).isEqualTo(10);
	}

	@TestFactory
	Stream<DynamicTest> testNumberSequence() {
		var generator = Generators.number();
		return IntStream.range(0, 10)
				.mapToObj(value -> dynamicTest("get() = " + value,
						() -> assertThat(generator.get()).isEqualTo(value)));
	}

	@Test
	void testStr() {
		assertThat(Generators.str(10, 6).get()).isEqualTo("AAAAAK");
	}

	@Test
	void testNoop() {
		assertThat(Generators.noop().get()).isNull();
	}

	@Test
	void testBase26Encode() {
		assertThat(Generators.base26Encode(0, 5))
				.as("zero -> AAAAA")
				.isEqualTo("AAAAA");
		assertThat(Generators.base26Encode(1882010, 5))
				.as("1882010 -> EDCBA")
				.isEqualTo("EDCBA");
		assertThatExceptionOfType(IllegalArgumentException.class)
				.as("1882010 into 4 digits!")
				.isThrownBy(() -> Generators.base26Encode(1882010, 4))
				.withMessage("Value 1882010 is too large to express as 4 base26 digits")
				.withNoCause();
	}
}