package one.xis.security;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginFormData {
    private String username;
    private String password;
    private String redirect;

    // Getters and setters can be added here if needed
}
