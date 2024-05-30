package uk.co.bluegecko.marine.test.base;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NonNull;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter(AccessLevel.PROTECTED)
@Accessors(fluent = true, makeFinal = true)
public abstract class MapperTest extends DatedTest {

	ObjectMapper jsonMapper;
	ObjectMapper xmlMapper;
	ObjectMapper ctorMapper;

	@SafeVarargs
	protected MapperTest(Class<? extends Module>... modules) {
		var customizer = customizer(modules);
		jsonMapper = customizeAndBuild(Jackson2ObjectMapperBuilder.json(), customizer);
		xmlMapper = customizeAndBuild(Jackson2ObjectMapperBuilder.xml(), customizer);
		ctorMapper = customizeAndBuild(Jackson2ObjectMapperBuilder.cbor(), customizer);
	}

	Jackson2ObjectMapperBuilderCustomizer customizer(Class<? extends Module>[] modules) {
		return b -> {
			b.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
			b.modulesToInstall(modules);
		};
	}

	protected ObjectMapper customizeAndBuild(@NonNull Jackson2ObjectMapperBuilder builder,
			@NonNull Jackson2ObjectMapperBuilderCustomizer customizer) {
		customizer.customize(builder);
		return builder.build();
	}

}