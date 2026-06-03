package one.xis.ddl;

import java.sql.Connection;
import java.sql.SQLException;
import javax.sql.DataSource;

public enum SqlDialect {
    H2,
    MARIADB,
    POSTGRESQL;

    static SqlDialect detect(DataSource dataSource) {
        try (Connection connection = dataSource.getConnection()) {
            return detect(connection);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not detect SQL dialect", e);
        }
    }

    static SqlDialect detect(Connection connection) {
        try {
            String productName = connection.getMetaData().getDatabaseProductName().toLowerCase();
            if (productName.contains("h2")) {
                return H2;
            }
            if (productName.contains("mariadb") || productName.contains("mysql")) {
                return MARIADB;
            }
            if (productName.contains("postgres")) {
                return POSTGRESQL;
            }
            throw new IllegalArgumentException("Unsupported database product: " + productName);
        } catch (SQLException e) {
            throw new IllegalStateException("Could not detect SQL dialect", e);
        }
    }
}
