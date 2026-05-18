package one.xis;

/**
 * Application-wide upload limits.
 * <p>
 * XIS provides a default implementation. Host frameworks such as Spring may provide
 * their own implementation that reads the same {@code xis.upload.*} properties.
 * <p>
 * The two limits have different jobs:
 * <ul>
 *     <li>{@link #getMaxRequestSize()} is the early transport-level guard for the complete multipart request.</li>
 *     <li>{@link #getMaxFileSize()} is the default validation limit for one uploaded file.</li>
 * </ul>
 * A field or controller parameter annotated with {@link Upload#maxSize()} can use a more specific per-file validation
 * limit. Keep the request limit high enough for the largest valid form submission if you want XIS to return normal
 * validation messages for oversized files. If the complete HTTP request exceeds {@code getMaxRequestSize()}, the request
 * may be rejected before controller validation runs.
 */
@ImportInstances
public interface UploadConfiguration {

    long DEFAULT_MAX_FILE_SIZE = 10L * 1024L * 1024L;
    long DEFAULT_MAX_REQUEST_SIZE = 25L * 1024L * 1024L;

    default long getMaxFileSize() {
        return DEFAULT_MAX_FILE_SIZE;
    }

    default long getMaxRequestSize() {
        return DEFAULT_MAX_REQUEST_SIZE;
    }

    static long parseSize(String value, long defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        String normalized = value.trim().toUpperCase();
        long multiplier = 1;
        if (normalized.endsWith("KB")) {
            multiplier = 1024L;
            normalized = normalized.substring(0, normalized.length() - 2).trim();
        } else if (normalized.endsWith("MB")) {
            multiplier = 1024L * 1024L;
            normalized = normalized.substring(0, normalized.length() - 2).trim();
        } else if (normalized.endsWith("GB")) {
            multiplier = 1024L * 1024L * 1024L;
            normalized = normalized.substring(0, normalized.length() - 2).trim();
        } else if (normalized.endsWith("B")) {
            normalized = normalized.substring(0, normalized.length() - 1).trim();
        }
        return Long.parseLong(normalized) * multiplier;
    }
}
