package syos.exceptions;

/**
 * Custom exception for data access operations
 * Following Single Responsibility Principle - only handles data access errors
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
