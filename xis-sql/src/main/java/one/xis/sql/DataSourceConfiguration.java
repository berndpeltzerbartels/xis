package one.xis.sql;

import one.xis.context.Bean;
import one.xis.context.Component;

import javax.sql.DataSource;
import java.util.Collection;

@Component
class DataSourceConfiguration {

    @Bean
    DataSourceProvider dataSourceProvider(Collection<DataSource> dataSources, DataSourceFactory dataSourceFactory) {
        if (dataSources.isEmpty()) {
            return new DataSourceProvider(dataSourceFactory.dataSource());
        }
        if (dataSources.size() == 1) {
            return new DataSourceProvider(dataSources.iterator().next());
        }
        throw new IllegalStateException("More than one SQL DataSource is available");
    }
}
