package one.xis.auth.sql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.xis.sql.Entity;

@Entity("xis_idp_user_credentials")
@Data
@NoArgsConstructor
@AllArgsConstructor
class IDPUserCredentialsRecord {

    private String userId;
    private String passwordHash;
}
