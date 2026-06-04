package one.xis.auth.sql;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import one.xis.sql.Entity;

@Entity("xis_local_credentials")
@Data
@NoArgsConstructor
@AllArgsConstructor
class LocalCredentialsRecord {

    private String userId;
    private String passwordHash;
}
