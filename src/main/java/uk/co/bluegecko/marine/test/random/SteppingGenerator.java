package uk.co.bluegecko.marine.test.random;

import java.util.Random;
import java.util.random.RandomGenerator;

public class SteppingGenerator extends Random implements RandomGenerator {
	private long sequentialNumber;
	private final int min;
	private final int max;
	private final int step;

	public SteppingGenerator(int min, int max, int step) {
		this.min = min;
		this.max = max;
		this.step = step;
		sequentialNumber = min;
	}

	public SteppingGenerator() {
		this(0, Integer.MAX_VALUE, 1);
	}

	@Override
	public int next(int bits) {
		long result = sequentialNumber;
		sequentialNumber += step;
		if (sequentialNumber >= max) sequentialNumber = min;
		return (int) (result & Long.MAX_VALUE >> (Long.SIZE - (1 + bits)));
	}

	@Override
	public long nextLong() {
		return nextInt();
	}

	@Override
	public float nextFloat() {
		return ((float) (nextInt() % 10)) / 10.0f;
	}

	@Override
	public double nextDouble() {
		return nextFloat();
	}

}
