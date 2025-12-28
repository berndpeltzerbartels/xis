package one.xis.theme.example.contact;

import one.xis.validation.EMail;
import one.xis.validation.LabelKey;
import one.xis.validation.Mandatory;

import java.time.LocalDate;
import java.util.List;

/**
 * Contact record demonstrating XIS validation with records.
 * Tests: @Mandatory, @EMail, custom @PhoneNumber validation, and @LabelKey.
 * 
 * @LabelKey uses context-specific keys ("contact-email" vs generic "email")
 * to demonstrate how different contexts can have different field labels.
 */
public record Contact(
    Long id,
    
    @Mandatory
    @LabelKey("contact-firstName")
    String firstName,
    
    @Mandatory
    @LabelKey("contact-lastName")
    String lastName,
    
    @Mandatory
    @EMail
    @LabelKey("contact-email")
    String email,
    
    @PhoneNumber
    @LabelKey("contact-phone")
    String phone,
    
    String company,
    String position,
    
    @Mandatory
    ContactType type,
    
    @Mandatory
    ContactStatus status,
    
    String street,
    String city,
    String zipCode,
    String country,
    List<String> tags,
    LocalDate createdAt,
    String notes
) {
    /**
     * Default constructor for form binding.
     * Creates an empty Contact with all fields null.
     */
    public Contact() {
        this(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
    }
    
    public String getFullName() {
        return firstName + " " + lastName;
    }
}
