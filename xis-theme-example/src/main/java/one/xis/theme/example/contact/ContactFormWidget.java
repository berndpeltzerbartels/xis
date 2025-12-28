package one.xis.theme.example.contact;

import lombok.RequiredArgsConstructor;
import one.xis.*;

import java.util.Arrays;
import java.util.List;

@Widget
@RequiredArgsConstructor
public class ContactFormWidget {

    private final ContactService contactService;

    @ModelData
    public List<ContactType> contactTypes() {
        return Arrays.asList(ContactType.values());
    }

    @ModelData
    public List<ContactStatus> contactStatuses() {
        return Arrays.asList(ContactStatus.values());
    }

    @ModelData
    public List<String> availableTags() {
        return Arrays.asList("VIP", "Priority", "New", "Hot Lead", "Follow-up", "Partner");
    }

    @ModelData
    public List<String> countries() {
        return Arrays.asList("United States", "Canada", "United Kingdom", "Germany", "France", "Spain");
    }

    @FormData("contact")
    public Contact contact(@WidgetParameter("contactId") @NullAllowed Long contactId) {
        if (contactId != null) {
            return contactService.findById(contactId).orElseGet(Contact::new);
        }
        return new Contact();
    }

    @Action("save")
    public Class<?> saveContact(@FormData("contact") Contact contact) {
        contactService.save(contact);
        return ContactListWidget.class;
    }

    @Action("cancel")
    public Class<?> cancel() {
        return ContactListWidget.class;
    }
}
