package one.xis.sql;

import lombok.RequiredArgsConstructor;
import one.xis.context.Component;
import one.xis.context.Init;

import javax.sql.DataSource;

@Component
@RequiredArgsConstructor
class DataSourceValidator {
    private final DataSource dataSource;

    @Init
    void validate() {
        if (dataSource instanceof SimpleDataSource simpleDataSource) {
            simpleDataSource.validateConfiguration();
        }
    }
}
