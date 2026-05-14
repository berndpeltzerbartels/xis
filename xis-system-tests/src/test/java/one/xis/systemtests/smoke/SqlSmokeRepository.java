package one.xis.systemtests.smoke;

import one.xis.sql.Select;

@SqlSmokeRepositoryProxy
interface SqlSmokeRepository {
    @Select("select 1")
    int one();
}
