package one.xis.sql;

import java.util.List;
import java.util.Optional;

/**
 * Generic repository operations for entities with a single-column primary key.
 *
 * @param <E> entity type annotated with {@link Entity}
 * @param <ID> primary-key type
 */
public interface CrudRepository<E, ID> {

    Optional<E> findById(ID id);

    List<E> findAll();

    E save(E entity);

    boolean delete(E entity);

    boolean deleteById(ID id);

    long count();
}
