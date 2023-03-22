package uk.co.bluegecko.marine.test.data;

import lombok.experimental.UtilityClass;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Supplier;

@UtilityClass
public class Generators {

	public static Supplier<UUID> uuid(long most, long least) {
		return new UUIDGenerator(most, least);
	}

	public static Supplier<UUID> uuid() {
		return uuid(0, 0);
	}

	public static Supplier<Long> number(long initial) {
		return new LongGenerator(initial);
	}

	public static Supplier<Long> number() {
		return number(0);
	}

	public static Supplier<String> str(long initial, int length) {
		return new StringGenerator(initial, length);
	}

	public static Supplier<String> str() {
		return str(0, 6);
	}

	public static <T> Supplier<T> noop() {
		return () -> null;
	}

	private static class UUIDGenerator implements Supplier<UUID> {

		private final long most;
		private final AtomicLong least;

		private UUIDGenerator(final long most, final long least) {
			this.most = most;
			this.least = new AtomicLong(least);
		}

		@Override
		public UUID get() {
			return new UUID(most, least.getAndIncrement());
		}
	}

	private static class LongGenerator implements Supplier<Long> {

		private final AtomicLong next;

		private LongGenerator(final long initial) {
			this.next = new AtomicLong(initial);
		}

		@Override
		public Long get() {
			return next.getAndIncrement();
		}
	}

	private static class StringGenerator implements Supplier<String> {

		private final AtomicLong next;
		private final int length;

		private StringGenerator(final long initial, final int length) {
			this.next = new AtomicLong(initial);
			this.length = length;
		}

		@Override
		public String get() {
			return base26Encode(next.getAndIncrement(), length);
		}
	}

	public static String base26Encode(final long value, final int length) {
		long v = value;
		StringBuilder buffer = new StringBuilder();
		for (int i = length; i >= 0; i--) {
			int x = (int) Math.pow(26, i);
			int multiple = (int) v / x;
			if (i == length) {
				if (multiple > 0) throw new IllegalArgumentException(
						String.format("Value %d is too large to express as %d base26 digits", value, length));
			} else {
				v -= x * multiple;
				char ch = Character.valueOf((char) (multiple + 65));
				buffer.append(ch);
			}
		}
		return buffer.toString();
	}
}
