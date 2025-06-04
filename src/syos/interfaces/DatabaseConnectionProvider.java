package syos.interfaces;

import java.sql.Connection;

/**
 * Database Connection Provider interface for Dependency Inversion Principle
 */
public interface DatabaseConnectionProvider {
    Connection getConnection() throws Exception;

    Connection getPoolConnection() throws Exception;
}
