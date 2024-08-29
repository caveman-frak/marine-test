package org.junit.jupiter.params.provider;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Stream;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.ReflectionUtils;
import uk.co.bluegecko.marine.test.junit.InputOutputSource;

public class InputOutputArgumentsProvider extends AnnotationBasedArgumentsProvider<InputOutputSource> {

	@Override
	protected Stream<? extends Arguments> provideArguments(ExtensionContext context, InputOutputSource annotation) {
		Stream<? extends Arguments> inputStream = stream(annotation.inputs(), context);
		Stream<? extends Arguments> outputStream = stream(annotation.outputs(), context);

		Iterator<? extends Arguments> inputIt = inputStream.iterator();
		Iterator<? extends Arguments> outputIt = outputStream.iterator();

		List<Arguments> merged = new ArrayList<>();
		while (inputIt.hasNext() && outputIt.hasNext()) {
			Arguments inputArgs = inputIt.next();
			Arguments outputArgs = outputIt.next();

			Object[] combined = new Object[inputArgs.get().length + outputArgs.get().length];
			System.arraycopy(inputArgs.get(), 0, combined, 0, inputArgs.get().length);
			System.arraycopy(outputArgs.get(), 0, combined, inputArgs.get().length, outputArgs.get().length);
			merged.add(Arguments.of(combined));
		}
		if (inputIt.hasNext() != outputIt.hasNext()) {
			throw new IllegalArgumentException("Size mismatch between input and output argument providers");
		}
		return merged.stream();
	}

	Stream<? extends Arguments> stream(CsvSource source, ExtensionContext context) {
		CsvArgumentsProvider provider = ReflectionUtils.newInstance(CsvArgumentsProvider.class);
		return provider.provideArguments(context, source);
	}

	Stream<? extends Arguments> stream(ValueSource source, ExtensionContext context) {
		ValueArgumentsProvider provider = ReflectionUtils.newInstance(ValueArgumentsProvider.class);
		return provider.provideArguments(context, source);
	}

}