package one.xis.mongodb;

import java.util.List;
import java.util.Optional;

/**
 * Small generic repository API for MongoDB documents.
 *
 * <p>Repository interfaces extend this type and are annotated with
 * {@link MongoRepository}. Custom lookup methods can be added with
 * {@link MongoQuery}.</p>
 *
 * @param <E>  document type
 * @param <ID> id type
 */
public interface MongoCrudRepository<E, ID> {
    Optional<E> findById(ID id);

    List<E> findAll();

    E save(E entity);

    boolean delete(E entity);

    boolean deleteById(ID id);

    long count();
}
