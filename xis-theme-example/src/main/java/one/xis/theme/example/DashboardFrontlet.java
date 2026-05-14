package one.xis.theme.example;

import lombok.RequiredArgsConstructor;
import one.xis.ModelData;
import one.xis.Frontlet;
import one.xis.theme.example.contact.ContactService;
import one.xis.theme.example.contact.ContactType;

@Frontlet
@RequiredArgsConstructor
public class DashboardFrontlet {

    private final ContactService contactService;

    @ModelData
    public DashboardStats stats() {
        return new DashboardStats(
                contactService.count(),
                contactService.countByType(ContactType.CUSTOMER),
                contactService.countByType(ContactType.LEAD),
                15 // Mock data for activities
        );
    }

    public record DashboardStats(
            long totalContacts,
            long customers,
            long leads,
            int recentActivities
    ) {
    }
}
