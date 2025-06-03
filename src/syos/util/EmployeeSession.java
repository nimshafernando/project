package syos.util;

/**
 * Singleton class to manage the currently logged-in employee session.
 * Integrates with existing EmployeeMainMenuController authentication.
 */
public class EmployeeSession {
    private static EmployeeSession instance;
    private String currentEmployeeName;
    private String employeeId;
    private String role;
    private boolean isLoggedIn;

    private EmployeeSession() {
        this.isLoggedIn = false;
    }

    public static synchronized EmployeeSession getInstance() {
        if (instance == null) {
            instance = new EmployeeSession();
        }
        return instance;
    }

    /**
     * Called by EmployeeMainMenuController after successful authentication
     */
    public void loginEmployee(String employeeName, String employeeId, String role) {
        this.currentEmployeeName = employeeName;
        this.employeeId = employeeId;
        this.role = role;
        this.isLoggedIn = true;
        System.out.println("Session started for: " + employeeName);
    }

    /**
     * Logs out the current employee
     */
    public void logout() {
        if (isLoggedIn) {
            System.out.println("Session ended for: " + currentEmployeeName);
        }
        this.currentEmployeeName = null;
        this.employeeId = null;
        this.role = null;
        this.isLoggedIn = false;
    }

    /**
     * Gets the current logged-in employee's name for bill creation
     */
    public String getCurrentEmployeeName() {
        return isLoggedIn ? currentEmployeeName : null;
    }

    public String getEmployeeId() {
        return isLoggedIn ? employeeId : null;
    }

    public String getRole() {
        return isLoggedIn ? role : null;
    }

    public boolean isLoggedIn() {
        return isLoggedIn;
    }

    /**
     * Validates session for bill creation
     */
    public void validateSession() {
        if (!isLoggedIn || currentEmployeeName == null) {
            throw new IllegalStateException(
                    "No employee is currently logged in. Please authenticate through the employee portal.");
        }
    }

    /**
     * Gets formatted employee info for display
     */
    public String getEmployeeInfo() {
        if (!isLoggedIn) {
            return "No employee logged in";
        }
        return String.format("%s (%s) - %s", currentEmployeeName, employeeId, role);
    }
}