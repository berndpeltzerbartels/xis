package one.xis.theme.example.contact;

import lombok.RequiredArgsConstructor;
import one.xis.*;

import java.util.List;

@Widget
@RequiredArgsConstructor
public class ContactListWidget {

    private final ContactService contactService;

    @ModelData
    public List<Contact> contacts() {
        return contactService.findAll();
    }

    @ModelData
    public long totalCount() {
        return contactService.count();
    }

    @ModelData
    public long leadsCount() {
        return contactService.countByType(ContactType.LEAD);
    }

    @ModelData
    public long customersCount() {
        return contactService.countByType(ContactType.CUSTOMER);
    }

    @Action("edit")
    public WidgetResponse editContact(@ActionParameter("contactId") Long contactId) {
        return WidgetResponse.of(ContactFormWidget.class, "contactId", contactId);
    }

    @Action("delete")
    public void deleteContact(@ActionParameter("contactId") Long contactId) {
        contactService.delete(contactId);
    }
}
