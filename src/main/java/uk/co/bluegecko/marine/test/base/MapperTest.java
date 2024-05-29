package uk.co.bluegecko.marine.test.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.Accessors;
import lombok.experimental.FieldDefaults;

@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
@Getter(AccessLevel.PROTECTED)
@Accessors(fluent = true, makeFinal = true)
public abstract class MapperTest extends DatedTest {

	ObjectMapper objectMapper;

	protected MapperTest() {
		super();
		objectMapper = JsonMapper.builder()
				.defaultLeniency(true)
				.addModule(new JavaTimeModule())
				.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
				.build();
	}

}