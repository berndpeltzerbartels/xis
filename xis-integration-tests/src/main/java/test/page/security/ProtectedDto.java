package test.page.security;

import one.xis.Roles;

@Roles({"DATA_EDITOR", "CONTENT_MANAGER"})
public class ProtectedDto {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
