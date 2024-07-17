package uk.co.bluegecko.marine.test.jassert;

import java.math.BigDecimal;
import java.math.BigInteger;
import org.assertj.core.presentation.StandardRepresentation;

public class NumberRepresentation extends StandardRepresentation {

	@Override
	protected String toStringOf(Number n) {
		return switch (n) {
			case Byte b -> toStringOf(b.intValue());
			case Short s -> toStringOf(s.intValue());
			case Integer i -> toStringOf(i);
			case Long l -> toStringOf(l);
			case BigInteger i -> toStringOf(i.longValue());
			case Float f -> toStringOf(f);
			case Double d -> toStringOf(d);
			case BigDecimal d -> toStringOf(d.doubleValue());
			case null -> null;
			default -> toStringOf(n.doubleValue());
		};
	}

	protected String toStringOf(Double d) {
		return String.format("%1.16f", d);
	}

	@Override
	protected String toStringOf(Float f) {
		return String.format("%1.16ff", f);
	}

	protected String toStringOf(Integer i) {
		return String.format("%1d", i);
	}

	@Override
	protected String toStringOf(Long l) {
		return String.format("%1dL", l);
	}

}