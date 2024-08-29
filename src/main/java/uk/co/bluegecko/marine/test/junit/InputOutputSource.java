package uk.co.bluegecko.marine.test.junit;

import static org.apiguardian.api.API.Status.STABLE;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.apiguardian.api.API;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.InputOutputArgumentsProvider;
import org.junit.jupiter.params.provider.ValueSource;

@Target({ElementType.ANNOTATION_TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@API(status = STABLE, since = "5.7")
@ArgumentsSource(InputOutputArgumentsProvider.class)
public @interface InputOutputSource {

	CsvSource inputs();

	ValueSource outputs();

}