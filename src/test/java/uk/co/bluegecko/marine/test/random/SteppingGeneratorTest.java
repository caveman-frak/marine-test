package uk.co.bluegecko.marine.test.random;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

class SteppingGeneratorTest {

	@TestFactory
	Stream<DynamicTest> testNextIntSimple() {
		var generator = new SteppingGenerator();
		return IntStream.range(0, 10)
				.mapToObj(value -> dynamicTest("nextInt() = " + value,
						() -> assertThat(generator.nextInt()).isEqualTo(value)));
	}

	@TestFactory
	Stream<DynamicTest> testNextIntStepped() {
		var generator = new SteppingGenerator(10, 20, 2);
		return IntStream.of(10, 12, 14, 16, 18, 10, 12, 14, 16, 18, 10)
				.mapToObj(value -> dynamicTest("nextInt() = " + value,
						() -> assertThat(generator.nextInt()).isEqualTo(value)));
	}

	@TestFactory
	Stream<DynamicTest> testNextIntBound() {
		var generator = new SteppingGenerator();
		return IntStream.of(1, 1, 2, 2, 3, 3, 4, 4, 5, 5, 6, 6, 7, 7, 8, 8, 9, 9, 1, 1)
				.mapToObj(value -> dynamicTest("nextInt() = " + value,
						() -> assertThat(generator.nextInt(1, 10)).isEqualTo(value)));
	}

	@TestFactory
	Stream<DynamicTest> testNextBoolean() {
		var generator = new SteppingGenerator();
		return Stream.of(Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE, Boolean.FALSE, Boolean.TRUE)
				.map(value -> dynamicTest("nextBool()) = " + value,
						() -> assertThat(generator.nextBoolean()).isEqualTo(value)));
	}

	@TestFactory
	Stream<DynamicTest> testNextLong() {
		var generator = new SteppingGenerator();
		return LongStream.range(0, 10)
				.mapToObj(value -> dynamicTest("nextLong() = " + value,
						() -> assertThat(generator.nextLong()).isEqualTo(value)));
	}

	@TestFactory
	Stream<DynamicTest> testNextFloat() {
		var generator = new SteppingGenerator();
		return DoubleStream.of(0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0)
				.mapToObj(value -> dynamicTest("nextFloat() = " + value,
						() -> assertThat(generator.nextFloat()).isEqualTo((float) value)));
	}

	@TestFactory
	Stream<DynamicTest> testNextDouble() {
		var generator = new SteppingGenerator();
		return DoubleStream.of(0.0, 0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9, 0.0)
				.mapToObj(value -> dynamicTest("nextDouble() = " + value,
						() -> assertThat(generator.nextDouble()).isEqualTo(value, within(0.01))));
	}

}