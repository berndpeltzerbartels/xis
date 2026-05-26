package one.xis;

import java.lang.annotation.*;

/**
 * Binds a value from the browser's sessionStorage to an action method parameter.
 *
 * <p>This is a parameter annotation. XIS scans controller method parameters and
 * writes the referenced storage keys into the client configuration for the
 * current page or frontlet. The browser sends only those configured keys to the
 * server, not the whole sessionStorage. The configured keys are sent for the
 * page/frontlet request even when the currently invoked action does not use
 * every key.</p>
 *
 * <p>The server deserializes the value for the key, passes it to the parameter,
 * and writes the parameter value back to sessionStorage after method execution.
 * This makes the annotation most useful for mutable DTO-like values whose fields
 * are changed inside the action.</p>
 *
 * <p><strong>Example:</strong></p>
 * <pre>{@code
 * @Action("updateWizardStep")
 * public void updateStep(@SessionStorage("wizardData") WizardData data,
 *                        @ActionParameter("step") int step) {
 *     data.setCurrentStep(step);
 *     // the mutated data parameter is saved back to sessionStorage
 * }
 * }</pre>
 *
 * <p><strong>Initialization:</strong><br>
 * If no value exists in sessionStorage, the parameter will be initialized with a default value.
 * For objects, this is typically a new instance. Use {@link NullAllowed} to allow null values instead.</p>
 *
 * <p><strong>Storage Location:</strong><br>
 * Data is stored client-side in the browser's sessionStorage and persists only for the current
 * browser tab/window session. Data is cleared when the tab is closed. All values are serialized as JSON.</p>
 *
 * <p><strong>Difference from LocalStorage:</strong><br>
 * Unlike {@link LocalStorage}, sessionStorage data does not persist across browser sessions
 * and is isolated per tab/window.</p>
 *
 * @see LocalStorage
 * @see ClientStorage
 * @see NullAllowed
 */
@Documented
@Target({ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface SessionStorage {
    /**
     * The sessionStorage key to read from the browser and write back after
     * method execution.
     *
     * @return the storage key
     */
    String value();
}
