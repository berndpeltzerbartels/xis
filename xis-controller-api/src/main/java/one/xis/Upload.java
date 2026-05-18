package one.xis;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * Binds an uploaded file to a form field or controller parameter.
 * <p>
 * The value is the multipart field name. When it is omitted on a form object field,
 * the Java field name is used.
 * <p>
 * The annotated field or parameter may be {@link UploadedFile}, {@code List<UploadedFile>}, {@code byte[]}, or
 * {@link String}. String values are decoded as UTF-8.
 */
@Retention(RUNTIME)
@Target({FIELD, PARAMETER})
public @interface Upload {

    String value() default "";

    /**
     * Maximum accepted size for this single file in bytes. A negative value means
     * that {@link UploadConfiguration#getMaxFileSize()} is used.
     * <p>
     * This is a validation limit, not the complete multipart request limit. The transport-level request limit is
     * controlled by {@link UploadConfiguration#getMaxRequestSize()}.
     */
    long maxSize() default -1;
}
