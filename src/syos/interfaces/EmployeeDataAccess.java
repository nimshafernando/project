package syos.interfaces;

import syos.model.Employee;

/**
 * Employee data access interface following Interface Segregation Principle
 */
public interface EmployeeDataAccess {

    /**
     * Authenticate employee by ID and PIN
     * 
     * @param employeeId The employee ID
     * @param pin        The employee PIN
     * @return Employee object if authenticated, null otherwise
     */
    Employee authenticateEmployee(String employeeId, String pin);

    /**
     * Get employee by ID (without PIN verification)
     * 
     * @param employeeId The employee ID
     * @return Employee object if found, null otherwise
     */
    Employee getEmployeeById(String employeeId);

    /**
     * Check if employee ID exists
     * 
     * @param employeeId The employee ID
     * @return true if exists, false otherwise
     */
    boolean employeeExists(String employeeId);
}
