package one.xis.auth.sql;

import one.xis.sql.Repository;
import one.xis.sql.Param;
import one.xis.sql.Save;
import one.xis.sql.Select;

import java.util.Optional;

@Repository
interface LocalCredentialsSqlRepository {

    @Select("select user_id, password_hash from xis_local_credentials where user_id = {userId}")
    Optional<LocalCredentialsRecord> findByUserId(@Param("userId") String userId);

    @Save
    void save(LocalCredentialsRecord record);
}
