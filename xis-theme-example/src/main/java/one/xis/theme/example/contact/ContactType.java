package one.xis.theme.example.contact;


import lombok.Getter;

@Getter
public enum ContactType {
    LEAD("Lead"),
    CUSTOMER("Customer"),
    PARTNER("Partner"),
    SUPPLIER("Supplier");

    private final String displayName;

    ContactType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
