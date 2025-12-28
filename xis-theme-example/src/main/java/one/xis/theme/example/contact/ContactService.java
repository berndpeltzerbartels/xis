package one.xis.theme.example.contact;

import one.xis.context.XISComponent;

import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@XISComponent
public class ContactService {
    
    private final Map<Long, Contact> contacts = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(1);

    public ContactService() {
        // Initialize with sample data
        initializeSampleData();
    }

    public List<Contact> findAll() {
        return new ArrayList<>(contacts.values());
    }

    public Optional<Contact> findById(Long id) {
        return Optional.ofNullable(contacts.get(id));
    }

    public List<Contact> findByType(ContactType type) {
        return contacts.values().stream()
                .filter(c -> c.type() == type)
                .collect(Collectors.toList());
    }

    public List<Contact> findByStatus(ContactStatus status) {
        return contacts.values().stream()
                .filter(c -> c.status() == status)
                .collect(Collectors.toList());
    }

    public List<Contact> search(String query) {
        String lowerQuery = query.toLowerCase();
        return contacts.values().stream()
                .filter(c -> 
                    c.firstName().toLowerCase().contains(lowerQuery) ||
                    c.lastName().toLowerCase().contains(lowerQuery) ||
                    c.email().toLowerCase().contains(lowerQuery) ||
                    (c.company() != null && c.company().toLowerCase().contains(lowerQuery))
                )
                .collect(Collectors.toList());
    }

    public Contact save(Contact contact) {
        Contact savedContact = contact;
        if (contact.id() == null) {
            savedContact = new Contact(
                idGenerator.getAndIncrement(),
                contact.firstName(),
                contact.lastName(),
                contact.email(),
                contact.phone(),
                contact.company(),
                contact.position(),
                contact.type(),
                contact.status(),
                contact.street(),
                contact.city(),
                contact.zipCode(),
                contact.country(),
                contact.tags(),
                LocalDate.now(),
                contact.notes()
            );
        }
        contacts.put(savedContact.id(), savedContact);
        return savedContact;
    }

    public void delete(Long id) {
        contacts.remove(id);
    }

    public long count() {
        return contacts.size();
    }

    public long countByType(ContactType type) {
        return contacts.values().stream()
                .filter(c -> c.type() == type)
                .count();
    }

    private void initializeSampleData() {
        // Sample contacts
        save(createContact("John", "Doe", "john.doe@example.com", "+1-555-0101", 
            "Acme Corp", "CEO", ContactType.CUSTOMER, ContactStatus.ACTIVE));
        save(createContact("Jane", "Smith", "jane.smith@techstart.com", "+1-555-0102",
            "TechStart Inc", "CTO", ContactType.PARTNER, ContactStatus.ACTIVE));
        save(createContact("Bob", "Johnson", "bob.j@widgets.com", "+1-555-0103",
            "Widget Factory", "Sales Director", ContactType.LEAD, ContactStatus.PENDING));
        save(createContact("Alice", "Williams", "alice.w@designs.com", "+1-555-0104",
            "Creative Designs", "Creative Director", ContactType.CUSTOMER, ContactStatus.ACTIVE));
        save(createContact("Charlie", "Brown", "charlie@oldcorp.com", "+1-555-0105",
            "Old Corp", "Manager", ContactType.CUSTOMER, ContactStatus.INACTIVE));
    }

    private Contact createContact(String firstName, String lastName, String email, String phone,
                                 String company, String position, ContactType type, 
                                 ContactStatus status) {
        return new Contact(
            null, // id will be generated in save()
            firstName,
            lastName,
            email,
            phone,
            company,
            position,
            type,
            status,
            null, // street
            null, // city
            null, // zipCode
            null, // country
            new ArrayList<>(), // tags
            null, // createdAt will be set in save()
            null  // notes
        );
    }
}
