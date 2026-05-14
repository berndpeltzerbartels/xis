package one.xis.mongodb;

/**
 * Event object passed to {@link MongoWatch} methods when the method wants
 * metadata in addition to the changed document.
 *
 * @param type       mapped document type
 * @param collection MongoDB collection name
 * @param operation  MongoDB change stream operation name
 * @param document   changed document, or {@code null} if MongoDB did not provide a full document
 * @param <T>        mapped document type
 */
public record MongoChangeEvent<T>(Class<T> type, String collection, String operation, T document) {
}
