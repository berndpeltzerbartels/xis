package one.xis.validation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Specifies a custom label key for context-specific validation error messages.
 * 
 * <p>This annotation allows the same validation annotation to be used in different contexts
 * with different field labels in the resulting error messages. The label is looked up from
 * message properties files and can be used in validation message templates via the 
 * {@code ${label}} placeholder.</p>
 * 
 * <h2>Problem Statement</h2>
 * <p>When using validation annotations like {@code @Email}, {@code @Money}, or custom validators,
 * you often need the same validation logic in different contexts but with different field names
 * in error messages. Without {@code @LabelKey}, you would need to:</p>
 * <ul>
 *   <li>Create duplicate validation annotations for each context, or</li>
 *   <li>Use generic field names that don't match the business context</li>
 * </ul>
 * 
 * <h2>Solution</h2>
 * <p>{@code @LabelKey} decouples the validation logic from the presentation by allowing you to
 * specify a custom message property key that defines the field's label in error messages.</p>
 * 
 * <h2>Basic Example: Email Validation</h2>
 * <pre>{@code
 * public class CustomerForm {
 *     
 *     @Mandatory
 *     @Email
 *     private String email;  // Uses default label "email"
 *     
 *     @Mandatory
 *     @Email
 *     @LabelKey("secondaryEmail")  // Uses custom label "secondaryEmail"
 *     private String secondEmail;
 * }
 * }</pre>
 * 
 * <p><strong>Properties file (messages.properties):</strong></p>
 * <pre>
 * # Field labels
 * email=Email Address
 * secondaryEmail=Secondary Email Address
 * 
 * # Validation message template using ${label} placeholder
 * validation.email.invalid=The ${label} is not a valid email address
 * </pre>
 * 
 * <p><strong>Resulting error messages:</strong></p>
 * <ul>
 *   <li>{@code email} field: "The Email Address is not a valid email address"</li>
 *   <li>{@code secondEmail} field: "The Secondary Email Address is not a valid email address"</li>
 * </ul>
 * 
 * <h2>Advanced Example: Money Validation</h2>
 * <p>A more compelling use case is validating monetary amounts where the same validator
 * is used for different financial fields:</p>
 * 
 * <pre>{@code
 * public class ProductForm {
 *     
 *     @Mandatory
 *     @Money(maxValue = 1000000)
 *     @LabelKey("purchasePrice")
 *     private BigDecimal purchasePrice;
 *     
 *     @Mandatory
 *     @Money(maxValue = 100000)
 *     @LabelKey("salesTax")
 *     private BigDecimal salesTax;
 *     
 *     @Mandatory
 *     @Money(maxValue = 500000)
 *     @LabelKey("shippingCost")
 *     private BigDecimal shippingCost;
 * }
 * }</pre>
 * 
 * <p><strong>Properties file (messages.properties):</strong></p>
 * <pre>
 * # Field labels
 * purchasePrice=Purchase Price
 * salesTax=Sales Tax
 * shippingCost=Shipping Cost
 * 
 * # Validation message templates
 * validation.money.invalid=The ${label} is invalid
 * validation.money.exceeds=The ${label} exceeds the maximum value of ${max}
 * </pre>
 * 
 * <p><strong>Resulting error messages:</strong></p>
 * <ul>
 *   <li>{@code purchasePrice} error: "The Purchase Price is invalid"</li>
 *   <li>{@code salesTax} error: "The Sales Tax is invalid"</li>
 *   <li>{@code shippingCost} error: "The Shipping Cost is invalid"</li>
 * </ul>
 * 
 * <h2>How It Works</h2>
 * <ol>
 *   <li>When validation fails, the validator returns the message key (e.g., "validation.email.invalid")</li>
 *   <li>The validation framework looks up the message template from properties files</li>
 *   <li>If {@code @LabelKey} is present, its value is used to look up the field label from properties</li>
 *   <li>If {@code @LabelKey} is not present, the field name itself is used as the label key</li>
 *   <li>The {@code ${label}} placeholder in the message template is replaced with the resolved label</li>
 *   <li>Any additional variables provided by the validator (e.g., {@code ${max}}, {@code ${country}}) 
 *       are also replaced</li>
 * </ol>
 * 
 * <h2>Message Properties Resolution</h2>
 * <p>The validation framework searches for messages and labels in the following order:</p>
 * <ol>
 *   <li>Custom application properties: {@code messages.properties}, {@code messages_de.properties}, etc.</li>
 *   <li>Default XIS properties: {@code default-messages.properties}, {@code default-messages_de.properties}, etc.</li>
 * </ol>
 * 
 * <h2>Validator Custom Variables</h2>
 * <p>Validators can add their own variables to message templates. For example, a {@code @Money}
 * validator might add a {@code ${max}} variable for the maximum allowed value, or an {@code @Iban}
 * validator might add a {@code ${country}} variable for country-specific error messages.</p>
 * 
 * <pre>{@code
 * // In validator implementation
 * public class MoneyValidator {
 *     public boolean isValid(BigDecimal value) {
 *         if (value.compareTo(maxValue) > 0) {
 *             // Validator can provide additional variables
 *             addMessageVariable("max", maxValue.toString());
 *             return false;
 *         }
 *         return true;
 *     }
 * }
 * }</pre>
 * 
 * <h2>Internationalization</h2>
 * <p>Label keys work seamlessly with internationalization. Create separate properties files
 * for each locale:</p>
 * <ul>
 *   <li>{@code messages.properties} - Default (English) labels and messages</li>
 *   <li>{@code messages_de.properties} - German labels and messages</li>
 *   <li>{@code messages_fr.properties} - French labels and messages</li>
 * </ul>
 * 
 * <p>The validation framework automatically selects the appropriate file based on the
 * user's locale from {@code UserContext.getLocale()}.</p>
 * 
 * <p><strong>Example German properties (messages_de.properties):</strong></p>
 * <pre>
 * # Field labels
 * purchasePrice=Einkaufspreis
 * salesTax=Umsatzsteuer
 * 
 * # Validation messages
 * validation.money.invalid=Der angegebene ${label} ist ungültig
 * </pre>
 * 
 * <h2>Benefits</h2>
 * <ul>
 *   <li><strong>Reusability:</strong> Same validation annotation can be used in different contexts</li>
 *   <li><strong>Maintainability:</strong> Validation logic stays in one place, labels are external</li>
 *   <li><strong>Meaningful messages:</strong> Error messages use business-appropriate field names</li>
 *   <li><strong>Internationalization:</strong> Labels are defined per locale in properties files</li>
 *   <li><strong>Flexibility:</strong> Validators can add custom variables for richer messages</li>
 *   <li><strong>DRY principle:</strong> Don't duplicate validation logic for contextual differences</li>
 * </ul>
 * 
 * <h2>Usage on Action Parameters</h2>
 * <p>{@code @LabelKey} can also be used with {@code @ActionParameter} for validating action method parameters:</p>
 * 
 * <pre>{@code
 * @Action
 * void transferMoney(
 *     @ActionParameter("amount")
 *     @Money(maxValue = 10000)
 *     @LabelKey("transferAmount")
 *     BigDecimal amount
 * ) {
 *     // Process transfer
 * }
 * }</pre>
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
public @interface LabelKey {
    String value();
}
