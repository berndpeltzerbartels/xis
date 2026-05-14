package one.xis.spring;

import one.xis.sql.SimpleDataSource;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "one.xis.sql.SimpleDataSource")
class SpringSqlDataSourceConfiguration {

    @Bean
    @ConditionalOnMissingBean(DataSource.class)
    SimpleDataSource xisSqlDataSource(Environment environment) {
        var dataSource = new SimpleDataSource();
        dataSource.setUrl(environment.getProperty("xis.sql.url"));
        dataSource.setUser(environment.getProperty("xis.sql.user"));
        dataSource.setUsername(environment.getProperty("xis.sql.username"));
        dataSource.setPassword(environment.getProperty("xis.sql.password"));
        dataSource.setDriverClassName(environment.getProperty("xis.sql.driver-class-name"));
        dataSource.setPoolEnabled(environment.getProperty("xis.sql.pool.enabled", Boolean.class));
        dataSource.setMaximumPoolSize(environment.getProperty("xis.sql.pool.maximum-pool-size", Integer.class));
        dataSource.setMinimumIdle(environment.getProperty("xis.sql.pool.minimum-idle", Integer.class));
        dataSource.setConnectionTimeout(environment.getProperty("xis.sql.pool.connection-timeout", Long.class));
        dataSource.setIdleTimeout(environment.getProperty("xis.sql.pool.idle-timeout", Long.class));
        dataSource.setMaxLifetime(environment.getProperty("xis.sql.pool.max-lifetime", Long.class));
        dataSource.validateConfiguration();
        return dataSource;
    }
}
