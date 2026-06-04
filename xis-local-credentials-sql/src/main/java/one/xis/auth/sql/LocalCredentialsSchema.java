package one.xis.auth.sql;

import one.xis.ddl.Change;
import one.xis.ddl.ChangeSet;
import one.xis.ddl.DDL;

@ChangeSet("xis-local-credentials")
class LocalCredentialsSchema {

    @Change("001-create-local-credentials")
    void createLocalCredentials(DDL ddl) {
        var table = ddl.createTableIfNotExists("xis_local_credentials");
        table.addColumn("user_id").varchar(255).notNull().primaryKey();
        table.addColumn("password_hash").text().notNull();
    }
}
