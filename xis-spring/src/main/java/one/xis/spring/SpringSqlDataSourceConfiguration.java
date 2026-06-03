package one.xis.spring;

import one.xis.sql.DataSourceFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.sql.DataSource;

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(name = "one.xis.sql.DataSourceFactory")
class SpringSqlDataSourceConfiguration {

    @Bean
    @ConditionalOnMissingBean(DataSource.class)
    DataSource xisSqlDataSource(Environment environment) {
        var factory = new DataSourceFactory();
        factory.setUrl(environment.getProperty("xis.sql.url"));
        factory.setUser(environment.getProperty("xis.sql.user"));
        factory.setUsername(environment.getProperty("xis.sql.username"));
        factory.setPassword(environment.getProperty("xis.sql.password"));
        factory.setDriverClassName(environment.getProperty("xis.sql.driver-class-name"));
        factory.setPoolEnabled(environment.getProperty("xis.sql.pool.enabled", Boolean.class));
        factory.setMaximumPoolSize(environment.getProperty("xis.sql.pool.maximum-pool-size", Integer.class));
        factory.setMinimumIdle(environment.getProperty("xis.sql.pool.minimum-idle", Integer.class));
        factory.setConnectionTimeout(environment.getProperty("xis.sql.pool.connection-timeout", Long.class));
        factory.setIdleTimeout(environment.getProperty("xis.sql.pool.idle-timeout", Long.class));
        factory.setMaxLifetime(environment.getProperty("xis.sql.pool.max-lifetime", Long.class));
        return factory.dataSource();
    }
}
