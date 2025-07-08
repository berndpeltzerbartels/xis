package one.xis.auth.token;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApiTokensAndUrl {
    private ApiTokens apiTokens;
    private String url;
}
