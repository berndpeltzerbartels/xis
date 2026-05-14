package one.xis.theme.example.contact;

public enum ContactStatus {
    ACTIVE("Active"),
    INACTIVE("Inactive"),
    LOST("Lost"),
    PENDING("Pending");

    private final String displayName;

    ContactStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
