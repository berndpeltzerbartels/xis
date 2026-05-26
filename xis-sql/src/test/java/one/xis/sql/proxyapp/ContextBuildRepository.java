package one.xis.sql.proxyapp;

import one.xis.sql.Param;
import one.xis.sql.Repository;
import one.xis.sql.Select;

@Repository
public interface ContextBuildRepository {

    @Select("select first_name from people where id = {id}")
    String nameById(@Param("id") long id);
}
