package one.xis;

import java.lang.annotation.*;

/**
 * Binds a value from the browser's sessionStorage to an action method parameter.
 *
 * <p>The client sends only the specified sessionStorage key to the server.
 * The server deserializes the value, passes it to the method, and any modifications
 * made to the object are automatically saved back to sessionStorage after method execution.</p>
 *
 * <p><strong>Example:</strong></p>
 * <pre>{@code
 * @Action("updateWizardStep")
 * public void updateStep(@SessionStorage("wizardData") WizardData data,
 *                        @ActionParameter("step") int step) {
 *     data.setCurrentStep(step);
 *     // data is automatically saved back to sessionStorage
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
     * The sessionStorage key to read from and write to.
     *
     * @return the storage key
     */
    String value();
}
