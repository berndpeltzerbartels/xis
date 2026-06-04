package one.xis.auth.sql;

import one.xis.ddl.Change;
import one.xis.ddl.ChangeSet;
import one.xis.ddl.DDL;

@ChangeSet("xis-idp-credentials")
class IDPCredentialsSchema {

    @Change("001-create-idp-user-credentials")
    void createIDPUserCredentials(DDL ddl) {
        var table = ddl.createTableIfNotExists("xis_idp_user_credentials");
        table.addColumn("user_id").varchar(255).notNull().primaryKey();
        table.addColumn("password_hash").text().notNull();
    }

    @Change("002-create-idp-client-credentials")
    void createIDPClientCredentials(DDL ddl) {
        var table = ddl.createTableIfNotExists("xis_idp_client_credentials");
        table.addColumn("client_id").varchar(255).notNull().primaryKey();
        table.addColumn("client_secret_hash").text().notNull();
    }
}
