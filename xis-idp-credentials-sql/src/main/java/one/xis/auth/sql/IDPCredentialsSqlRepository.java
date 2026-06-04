package one.xis.auth.sql;

import one.xis.sql.Param;
import one.xis.sql.Repository;
import one.xis.sql.Save;
import one.xis.sql.Select;

import java.util.Optional;

@Repository
interface IDPCredentialsSqlRepository {

    @Select("select user_id, password_hash from xis_idp_user_credentials where user_id = {userId}")
    Optional<IDPUserCredentialsRecord> findUserById(@Param("userId") String userId);

    @Save
    void saveUser(IDPUserCredentialsRecord record);

    @Select("select client_id, client_secret_hash from xis_idp_client_credentials where client_id = {clientId}")
    Optional<IDPClientCredentialsRecord> findClientById(@Param("clientId") String clientId);

    @Save
    void saveClient(IDPClientCredentialsRecord record);
}
