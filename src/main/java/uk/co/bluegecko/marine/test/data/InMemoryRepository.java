package uk.co.bluegecko.marine.test.data;

import jakarta.persistence.EntityManager;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import lombok.NonNull;
import org.springframework.dao.IncorrectResultSizeDataAccessException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.FluentQuery;

public class InMemoryRepository<T, ID> implements JpaRepository<T, ID> {

	protected final Function<T, ID> extractor;
	protected final BiConsumer<ID, T> inserter;
	protected final Supplier<ID> generator;
	protected final Map<ID, T> entities = new ConcurrentHashMap<>();

	@SafeVarargs
	public InMemoryRepository(final Function<T, ID> extractor,
			final BiConsumer<ID, T> inserter,
			final Supplier<ID> generator,
			final T... entities) {
		this(extractor, inserter, generator, Arrays.stream(entities));
	}

	public InMemoryRepository(final Function<T, ID> extractor,
			final BiConsumer<ID, T> inserter,
			final Supplier<ID> generator,
			final Stream<T> entities) {
		this(extractor, inserter, generator,
				entities.collect(Collectors.toMap(extractor, e -> e)));
	}

	protected InMemoryRepository(final Function<T, ID> extractor,
			final BiConsumer<ID, T> inserter,
			final Supplier<ID> generator,
			final Map<ID, T> entities) {
		this(extractor, inserter, generator);
		this.entities.putAll(entities);
	}

	public InMemoryRepository(final Function<T, ID> extractor,
			final BiConsumer<ID, T> inserter,
			final Supplier<ID> generator) {
		this.extractor = extractor;
		this.inserter = inserter;
		this.generator = generator;
	}

	public InMemoryRepository<T, ID> populate(Stream<T> entities) {
		this.entities.putAll(entities.collect(Collectors.toMap(extractor, e -> e)));
		return this;
	}

	/**
	 * Saves a given entity. Use the returned instance for further operations as the save operation might have changed
	 * the entity instance completely.
	 *
	 * @param entity must not be {@literal null}.
	 * @return the saved entity; will never be {@literal null}.
	 * @throws IllegalArgumentException          in case the given {@literal entity} is {@literal null}.
	 * @throws OptimisticLockingFailureException when the entity uses optimistic locking and has a version attribute
	 *                                           with a different value from that found in the persistence store. Also
	 *                                           thrown if the entity is assumed to be present but does not exist in the
	 *                                           database.
	 */
	@Override
	public <S extends T> @NonNull S save(@NonNull S entity) {
		ID id = extractor.apply(entity);
		if (id == null) {
			id = generator != null ? generator.get() : null;
			if (id == null || inserter == null) {
				throw new IllegalArgumentException("Id must not be `null`");
			}
			if (inserter != null) {
				inserter.accept(id, entity);
			}
		}
		entities.put(id, entity);
		return entity;
	}

	/**
	 * Saves all given entities.
	 *
	 * @param entities must not be {@literal null} nor must it contain {@literal null}.
	 * @return the saved entities; will never be {@literal null}. The returned {@literal Iterable} will have the same
	 * size as the {@literal Iterable} passed as an argument.
	 * @throws IllegalArgumentException          in case the given {@link Iterable entities} or one of its entities is
	 *                                           {@literal null}.
	 * @throws OptimisticLockingFailureException when at least one entity uses optimistic locking and has a version
	 *                                           attribute with a different value from that found in the persistence
	 *                                           store. Also thrown if at least one entity is assumed to be present but
	 *                                           does not exist in the database.
	 */
	@Override
	public <S extends T> @NonNull List<S> saveAll(Iterable<S> entities) {
		List<S> result = new ArrayList<>();
		entities.forEach(e -> result.add(save(e)));
		return result;
	}

	/**
	 * Retrieves an entity by its id.
	 *
	 * @param id must not be {@literal null}.
	 * @return the entity with the given id or {@literal Optional#empty()} if none found.
	 * @throws IllegalArgumentException if {@literal id} is {@literal null}.
	 */
	@Override
	public @NonNull Optional<T> findById(@NonNull ID id) {
		return Optional.ofNullable(entities.get(id));
	}

	/**
	 * Returns whether an entity with the given id exists.
	 *
	 * @param id must not be {@literal null}.
	 * @return {@literal true} if an entity with the given id exists, {@literal false} otherwise.
	 * @throws IllegalArgumentException if {@literal id} is {@literal null}.
	 */
	@Override
	public boolean existsById(@NonNull ID id) {
		return entities.containsKey(id);
	}

	/**
	 * Returns all instances of the type.
	 *
	 * @return all entities
	 */
	@Override
	public @NonNull List<T> findAll() {
		return new ArrayList<>(entities.values());
	}

	/**
	 * Returns all instances of the type {@code T} with the given IDs.
	 * <p>
	 * If some or all ids are not found, no entities are returned for these IDs.
	 * <p>
	 * Note that the order of elements in the result is not guaranteed.
	 *
	 * @param ids must not be {@literal null} nor contain any {@literal null} values.
	 * @return guaranteed to be not {@literal null}. The size can be equal or less than the number of given
	 * {@literal ids}.
	 * @throws IllegalArgumentException in case the given {@link Iterable ids} or one of its items is {@literal null}.
	 */
	@Override
	public @NonNull List<T> findAllById(Iterable<ID> ids) {
		return StreamSupport.stream(ids.spliterator(), false)
				.map(entities::get).filter(Objects::nonNull)
				.toList();
	}

	/**
	 * Returns the number of entities available.
	 *
	 * @return the number of entities.
	 */
	@Override
	public long count() {
		return entities.size();
	}

	/**
	 * Deletes the entity with the given id.
	 * <p>
	 * If the entity is not found in the persistence store it is silently ignored.
	 *
	 * @param id must not be {@literal null}.
	 * @throws IllegalArgumentException in case the given {@literal id} is {@literal null}
	 */
	@Override
	public void deleteById(@NonNull ID id) {
		entities.remove(id);
	}

	/**
	 * Deletes a given entity.
	 *
	 * @param entity must not be {@literal null}.
	 * @throws IllegalArgumentException          in case the given entity is {@literal null}.
	 * @throws OptimisticLockingFailureException when the entity uses optimistic locking and has a version attribute
	 *                                           with a different value from that found in the persistence store. Also
	 *                                           thrown if the entity is assumed to be present but does not exist in the
	 *                                           database.
	 */
	@Override
	public void delete(@NonNull T entity) {
		entities.remove(extractor.apply(entity));
	}

	/**
	 * Deletes all instances of the type {@code T} with the given IDs.
	 * <p>
	 * Entities that aren't found in the persistence store are silently ignored.
	 *
	 * @param ids must not be {@literal null}. Must not contain {@literal null} elements.
	 * @throws IllegalArgumentException in case the given {@literal ids} or one of its elements is {@literal null}.
	 * @since 2.5
	 */
	@Override
	public void deleteAllById(Iterable<? extends ID> ids) {
		ids.forEach(this::deleteById);
	}

	/**
	 * Deletes the given entities.
	 *
	 * @param entities must not be {@literal null}. Must not contain {@literal null} elements.
	 * @throws IllegalArgumentException          in case the given {@literal entities} or one of its entities is
	 *                                           {@literal null}.
	 * @throws OptimisticLockingFailureException when at least one entity uses optimistic locking and has a version
	 *                                           attribute with a different value from that found in the persistence
	 *                                           store. Also thrown if at least one entity is assumed to be present but
	 *                                           does not exist in the database.
	 */
	@Override
	public void deleteAll(@NonNull Iterable<? extends T> entities) {
		entities.forEach(this::delete);
	}

	/**
	 * Deletes all entities managed by the repository.
	 */
	@Override
	public void deleteAll() {
		entities.clear();
	}

	/**
	 * Returns all entities sorted by the given options.
	 * <p>
	 * NOTE implementation will always return unsorted results.
	 *
	 * @param sort the {@link Sort} specification to sort the results by, can be {@link Sort#unsorted()}, must not be
	 *             {@literal null}.
	 * @return all entities sorted by the given options
	 */
	@Override
	public @NonNull List<T> findAll(@NonNull Sort sort) {
		return findAll();
	}

	/**
	 * Returns a {@link Page} of entities meeting the paging restriction provided in the {@link Pageable} object.
	 * <p>
	 * NOTE: implementation will always return un-paged results.
	 *
	 * @param pageable the pageable to request a paged result, can be {@link Pageable#unpaged()}, must not be
	 *                 {@literal null}.
	 * @return a page of entities
	 */
	@Override
	public @NonNull Page<T> findAll(@NonNull Pageable pageable) {
		return new PageImpl<>(findAll());
	}

	/**
	 * Returns a single entity matching the given {@link Example} or {@link Optional#empty()} if none was found.
	 * <p>
	 * NOTE implementation will always ignore the {@link Example}.
	 *
	 * @param example must not be {@literal null}.
	 * @return a single entity matching the given {@link Example} or {@link Optional#empty()} if none was found.
	 * @throws IncorrectResultSizeDataAccessException if the Example yields more than one result.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <S extends T> @NonNull Optional<S> findOne(@NonNull Example<S> example) {
		return (Optional<S>) entities.values().stream().findFirst();
	}

	/**
	 * Flushes all pending changes to the database.
	 * <p>
	 * NOTE: Flush is a No-Op for implementation.
	 */
	@Override
	public void flush() {
	}

	/**
	 * Saves an entity and flushes changes instantly.
	 * <p>
	 * NOTE: Flush is a No-Op for implementation.
	 *
	 * @param entity entity to be saved. Must not be {@literal null}.
	 * @return the saved entity
	 */
	@Override
	public <S extends T> @NonNull S saveAndFlush(@NonNull S entity) {
		return save(entity);
	}

	/**
	 * Saves all entities and flushes changes instantly.
	 * <p>
	 * NOTE: Flush is a No-Op for implementation.
	 *
	 * @param entities entities to be saved. Must not be {@literal null}.
	 * @return the saved entities
	 * @since 2.5
	 */
	@Override
	public <S extends T> @NonNull List<S> saveAllAndFlush(@NonNull Iterable<S> entities) {
		return saveAll(entities);
	}

	/**
	 * Deletes the given entities in a batch which means it will create a single query. This kind of operation leaves
	 * JPAs first level cache and the database out of sync. Consider flushing the {@link EntityManager} before calling
	 * this method.
	 * <p>
	 * NOTE: Batch is a No-Op for implementation.
	 *
	 * @param entities entities to be deleted. Must not be {@literal null}.
	 * @deprecated Use {@link #deleteAllInBatch(Iterable)} instead.
	 */
	@SuppressWarnings("deprecation")
	@Override
	@Deprecated
	public void deleteInBatch(@NonNull Iterable<T> entities) {
		deleteAll(entities);
	}

	/**
	 * Deletes the given entities in a batch which means it will create a single query. This kind of operation leaves
	 * JPAs first level cache and the database out of sync. Consider flushing the {@link EntityManager} before calling
	 * this method.
	 * <p>
	 * NOTE: Batch is a No-Op for implementation.
	 *
	 * @param entities entities to be deleted. Must not be {@literal null}.
	 * @since 2.5
	 */
	@Override
	public void deleteAllInBatch(@NonNull Iterable<T> entities) {
		deleteAll(entities);
	}

	/**
	 * Deletes the entities identified by the given ids using a single query. This kind of operation leaves JPAs first
	 * level cache and the database out of sync. Consider flushing the {@link EntityManager} before calling this
	 * method.
	 * <p>
	 * NOTE: Batch is a No-Op for implementation.
	 *
	 * @param ids the ids of the entities to be deleted. Must not be {@literal null}.
	 * @since 2.5
	 */
	@Override
	public void deleteAllByIdInBatch(@NonNull Iterable<ID> ids) {
		deleteAllById(ids);
	}

	/**
	 * Deletes all entities in a batch call.
	 * <p>
	 * NOTE: Batch is a No-Op for implementation.
	 */
	@Override
	public void deleteAllInBatch() {
		deleteAll();
	}

	/**
	 * Returns a reference to the entity with the given identifier. Depending on how the JPA persistence provider is
	 * implemented this is very likely to always return an instance and throw an
	 * {@link jakarta.persistence.EntityNotFoundException} on first access. Some of them will reject invalid identifiers
	 * immediately.
	 *
	 * @param id must not be {@literal null}.
	 * @return a reference to the entity with the given identifier.
	 * @see EntityManager#getReference(Class, Object) for details on when an exception is thrown.
	 * @deprecated use {@link JpaRepository#getReferenceById(ID)} instead.
	 */
	@Override
	@Deprecated
	public @NonNull T getOne(@NonNull ID id) {
		return getReferenceById(id);
	}

	/**
	 * Returns a reference to the entity with the given identifier. Depending on how the JPA persistence provider is
	 * implemented this is very likely to always return an instance and throw an
	 * {@link jakarta.persistence.EntityNotFoundException} on first access. Some of them will reject invalid identifiers
	 * immediately.
	 *
	 * @param id must not be {@literal null}.
	 * @return a reference to the entity with the given identifier.
	 * @see EntityManager#getReference(Class, Object) for details on when an exception is thrown.
	 * @since 2.5
	 * @deprecated use {@link JpaRepository#getReferenceById(ID)} instead.
	 */
	@Override
	@Deprecated
	public @NonNull T getById(@NonNull ID id) {
		return getReferenceById(id);
	}

	/**
	 * Returns a reference to the entity with the given identifier. Depending on how the JPA persistence provider is
	 * implemented this is very likely to always return an instance and throw an
	 * {@link jakarta.persistence.EntityNotFoundException} on first access. Some of them will reject invalid identifiers
	 * immediately.
	 *
	 * @param id must not be {@literal null}.
	 * @return a reference to the entity with the given identifier.
	 * @see EntityManager#getReference(Class, Object) for details on when an exception is thrown.
	 * @since 2.7
	 */
	@Override
	public @NonNull T getReferenceById(@NonNull ID id) {
		return Objects.requireNonNull(findById(id).orElse(null));
	}

	/**
	 * Returns all entities matching the given {@link Example}. In case no match could be found an empty
	 * {@link Iterable} is returned.
	 * <p>
	 * NOTE implementation will always ignore the {@link Example}.
	 *
	 * @param example must not be {@literal null}.
	 * @return all entities matching the given {@link Example}.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <S extends T> @NonNull List<S> findAll(@NonNull Example<S> example) {
		return (List<S>) findAll();
	}

	/**
	 * Returns all entities matching the given {@link Example} applying the given {@link Sort}. In case no match could
	 * be found an empty {@link Iterable} is returned.
	 * <p>
	 * NOTE implementation will always ignore the {@link Example}.
	 *
	 * @param example must not be {@literal null}.
	 * @param sort    the {@link Sort} specification to sort the results by, may be {@link Sort#unsorted()}, must not be
	 *                {@literal null}.
	 * @return all entities matching the given {@link Example}.
	 * @since 1.10
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <S extends T> @NonNull List<S> findAll(@NonNull Example<S> example, @NonNull Sort sort) {
		return (List<S>) findAll();
	}

	/**
	 * Returns a {@link Page} of entities matching the given {@link Example}. In case no match could be found, an empty
	 * {@link Page} is returned.
	 * <p>
	 * NOTE implementation will always ignore the {@link Example}.
	 *
	 * @param example  must not be {@literal null}.
	 * @param pageable the pageable to request a paged result, can be {@link Pageable#unpaged()}, must not be
	 *                 {@literal null}.
	 * @return a {@link Page} of entities matching the given {@link Example}.
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <S extends T> @NonNull Page<S> findAll(@NonNull Example<S> example,
			@NonNull Pageable pageable) {
		return (Page<S>) findAll(pageable);
	}

	/**
	 * Returns the number of instances matching the given {@link Example}.
	 * <p>
	 * NOTE implementation will always ignore the {@link Example}.
	 *
	 * @param example the {@link Example} to count instances for. Must not be {@literal null}.
	 * @return the number of instances matching the {@link Example}.
	 */
	@Override
	public <S extends T> long count(@NonNull Example<S> example) {
		return count();
	}

	/**
	 * Checks whether the data store contains elements that match the given {@link Example}.
	 * <p>
	 * NOTE implementation will always ignore the {@link Example}.
	 *
	 * @param example the {@link Example} to use for the existence check. Must not be {@literal null}.
	 * @return {@literal true} if the data store contains elements that match the given {@link Example}.
	 */
	@Override
	public <S extends T> boolean exists(@NonNull Example<S> example) {
		return !entities.isEmpty();
	}

	/**
	 * Returns entities matching the given {@link Example} applying the {@link Function queryFunction} that defines the
	 * query and its result type.
	 * <p>
	 * NOTE implementation will always ignore the {@link Example}.
	 *
	 * @param example       must not be {@literal null}.
	 * @param queryFunction the query function defining projection, sorting, and the result type
	 * @return all entities matching the given {@link Example}.
	 * @since 2.6
	 */
	@Override
	@SuppressWarnings("unchecked")
	public <S extends T, R> @NonNull R findBy(@NonNull Example<S> example,
			Function<FluentQuery.FetchableFluentQuery<S>, R> queryFunction) {
		return queryFunction.apply(
				(FluentQuery.FetchableFluentQuery<S>) findOne(example).orElseThrow());
	}

	public static <T, ID> BiConsumer<T, ID> noop() {
		return (e, i) -> {
		};
	}
}