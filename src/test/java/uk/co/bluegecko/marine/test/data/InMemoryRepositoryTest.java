package uk.co.bluegecko.marine.test.data;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class InMemoryRepositoryTest {

	@Data
	@AllArgsConstructor
	static final class Foo {
		private Long id;
		private String name;
	}

	private InMemoryRepository<Foo, Long> repository;

	@BeforeEach
	void setUp() {
		repository = new InMemoryRepository<>(Foo::id, (i, e) -> e.id(i), Generators.number(4),
				new Foo(1L, "One"), new Foo(2L, "Two"), new Foo(3L, "Three"));
	}

	@Test
	void testSaveNewWithId() {
		assertThat(repository.save(new Foo(5L, "Five")))
				.isEqualTo(new Foo(5L, "Five"));
		assertThat(repository.count()).isEqualTo(4);
		assertThat(repository.findById(5L))
				.isPresent().get()
				.isEqualTo(new Foo(5L, "Five"));
	}

	@Test
	void testSaveNewWithoutId() {
		assertThat(repository.save(new Foo(null, "Four")))
				.isEqualTo(new Foo(4L, "Four"));
		assertThat(repository.count()).isEqualTo(4);
		assertThat(repository.findById(4L))
				.isPresent().get()
				.isEqualTo(new Foo(4L, "Four"));
	}

	@Test
	void testSaveUpdated() {
		assertThat(repository.save(new Foo(1L, "One!")))
				.isEqualTo(new Foo(1L, "One!"));
		assertThat(repository.count()).isEqualTo(3);
		assertThat(repository.findById(1L))
				.isPresent().get()
				.isEqualTo(new Foo(1L, "One!"));
	}

	@Test
	void testSaveWithNullId() {
		// repository with `no-op` generator
		repository = new InMemoryRepository<>(Foo::id, (i, e) -> e.id(i), Generators.noop());

		// and entity with no id
		assertThatExceptionOfType(IllegalArgumentException.class)
				.isThrownBy(() -> repository.save(new Foo(null, "Four")))
				.withMessage("Id must not be `null`")
				.withNoCause();
	}

	@Test
	void testSaveAll() {
		assertThat(repository.saveAll(List.of(new Foo(null, "Four"), new Foo(null, "Five"))))
				.hasSize(2)
				.contains(new Foo(4L, "Four"), new Foo(5L, "Five"));
		assertThat(repository.count()).isEqualTo(5);
		assertThat(repository.findById(4L))
				.isPresent().get()
				.isEqualTo(new Foo(4L, "Four"));
		assertThat(repository.findById(5L))
				.isPresent().get()
				.isEqualTo(new Foo(5L, "Five"));
	}

	@Test
	void testFindById() {
		assertThat(repository.findById(2L))
				.as("Two")
				.isPresent().get()
				.isEqualTo(new Foo(2L, "Two"));
		assertThat(repository.findById(5L))
				.as("Five")
				.isEmpty();
	}

	@Test
	void testExistsById() {
		assertThat(repository.existsById(3L))
				.as("Three")
				.isTrue();
		assertThat(repository.existsById(6L))
				.as("Six")
				.isFalse();
	}

	@Test
	void testFindAll() {
		assertThat(repository.findAll())
				.hasSize(3)
				.extracting(Foo::name)
				.contains("One", "Two", "Three");
	}

	@Test
	void testFindAllById() {
		assertThat(repository.findAllById(List.of(1L, 3L)))
				.hasSize(2)
				.extracting(Foo::name)
				.contains("One", "Three");
	}

	@Test
	void testCount() {
		assertThat(repository.count())
				.isEqualTo(3);
	}

	@Test
	void testDeleteById() {
		assertThat(repository.existsById(2L))
				.as("Before")
				.isTrue();
		repository.deleteById(2L);
		assertThat(repository.existsById(2L))
				.as("After")
				.isFalse();
	}

	@Test
	void testDelete() {
		assertThat(repository.existsById(3L))
				.as("Before")
				.isTrue();
		repository.delete(new Foo(3L, "Three"));
		assertThat(repository.existsById(3L))
				.as("After")
				.isFalse();
	}

	@Test
	void testDeleteAllById() {
		repository.deleteAllById(List.of(1L, 3L));
		assertThat(repository.findAll())
				.hasSize(1)
				.extracting(Foo::name)
				.contains("Two");
	}

	@Test
	void testDeleteAll() {
		assertThat(repository.count())
				.as("Before")
				.isEqualTo(3L);
		repository.deleteAll();
		assertThat(repository.count())
				.as("After")
				.isEqualTo(0L);
	}

	@Test
	void testFindAllSorted() {
		// will always return unsorted results
		assertThat(repository.findAll(Sort.by(Sort.Direction.ASC, "name")))
				.hasSize(3)
				.extracting(Foo::name)
				.contains("One", "Two", "Three");
	}

	@Test
	void testFindAllPaged() {
		// will always return unpaged results
		assertThat(repository.findAll(PageRequest.of(0, 2)))
				.hasSize(3)
				.extracting(Foo::name)
				.contains("One", "Two", "Three");
	}

	@Test
	void testFindOneExample() {
		assertThat(repository.findOne(Example.of(new Foo(0L, null))))
				.isPresent().get()
				.isEqualTo(new Foo(1L, "One"));
	}

	@Test
	void testFindAllExample() {
		assertThat(repository.findAll(Example.of(new Foo(0L, null))))
				.hasSize(3)
				.extracting(Foo::name)
				.contains("One", "Two", "Three");
	}

	@Test
	void testFindAllExampleSorted() {
		assertThat(repository.findAll(Example.of(new Foo(0L, null)), Sort.by(Sort.Direction.ASC, "name")))
				.hasSize(3)
				.extracting(Foo::name)
				.contains("One", "Two", "Three");
	}

	@Test
	void testFindAllExamplePaged() {
		assertThat(repository.findAll(Example.of(new Foo(0L, null)), PageRequest.of(1, 2)))
				.hasSize(3)
				.extracting(Foo::name)
				.contains("One", "Two", "Three");
	}

	@Test
	void testCountExample() {
		assertThat(repository.count(Example.of(new Foo(0L, null))))
				.isEqualTo(3);
	}

	@Test
	void testExistsExample() {
		assertThat(repository.exists(Example.of(new Foo(0L, null))))
				.isTrue();
	}

}