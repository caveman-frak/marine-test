package uk.co.bluegecko.marine.test.jassert;

import org.assertj.core.configuration.Configuration;

public class StandardConfiguration extends Configuration {

	public StandardConfiguration() {
		super();

		setLenientDateParsing(true);
		setMaxLengthForSingleLineDescription(120);
	}
	
}