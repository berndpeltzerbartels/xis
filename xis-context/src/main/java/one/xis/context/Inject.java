package one.xis.context;

import java.lang.annotation.*;

/**
 * Marks a field for dependency injection. This is the XIS equivalent of Spring's {@code @Autowired}
 * or Java EE's {@code @Inject}.
 *
 * <p><strong>Note:</strong> Constructor injection is generally preferred over field injection for
 * better testability and immutability.</p>
 *
 * <p>Basic usage:</p>
 * <pre>
 * {@code @Page}("/users.html")
 * public class UsersPage {
 *     {@code @Inject}
 *     private UserService userService;
 * }
 * </pre>
 *
 * <p>Qualified injection (inject component annotated with specific annotation):</p>
 * <pre>
 * {@code @Inject}(annotatedWith = Primary.class)
 * private DataSource dataSource;
 * </pre>
 *
 * @see Component
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Inject {
    /**
     * Specifies an annotation that the injected component must be annotated with.
     * This allows selecting a specific implementation when multiple candidates exist.
     *
     * @return the annotation type to filter by, or {@link None} (default)
     */
    Class<? extends Annotation> annotatedWith() default None.class;
}
