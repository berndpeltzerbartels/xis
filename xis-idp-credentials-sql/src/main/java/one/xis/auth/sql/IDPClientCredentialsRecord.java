package one.xis.auth.sql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.xis.sql.Entity;

@Entity("xis_idp_client_credentials")
@Data
@NoArgsConstructor
@AllArgsConstructor
class IDPClientCredentialsRecord {

    private String clientId;
    private String clientSecretHash;
}
