package one.xis.context;


import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Marks a method as an event listener. When application events are emitted, all methods
 * annotated with {@code @EventListener} that accept the event type as a parameter will be invoked.
 * 
 * <p>The event type is determined by the method's single parameter type. Methods must have
 * exactly one parameter.</p>
 * 
 * <p>Example:</p>
 * <pre>
 * {@code @Component}
 * public class UserActivityLogger {
 *     
 *     {@code @EventListener}
 *     public void onUserLogin(UserLoginEvent event) {
 *         log.info("User logged in: " + event.getUserId());
 *     }
 *     
 *     {@code @EventListener}
 *     public void onUserLogout(UserLogoutEvent event) {
 *         log.info("User logged out: " + event.getUserId());
 *     }
 * }
 * </pre>
 * 
 * <p>To emit events, inject the {@code EventEmitter} and call its {@code emitEvent()} method:</p>
 * <pre>
 * {@code @Service}
 * public class AuthService {
 *     private final EventEmitter eventEmitter;
 *     
 *     public AuthService(EventEmitter eventEmitter) {
 *         this.eventEmitter = eventEmitter;
 *     }
 *     
 *     public void login(String userId) {
 *         // ... authentication logic ...
 *         eventEmitter.emitEvent(new UserLoginEvent(userId));
 *     }
 * }
 * </pre>
 * 
 * @see Component
 * @see EventEmitter
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface XISEventListener {
}
