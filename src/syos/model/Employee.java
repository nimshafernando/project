package syos.model;

/**
 * Employee model with authentication capabilities
 * Implements SOLID principles and follows your existing patterns
 */
public class Employee {
    private String employeeId;
    private String name;
    private String pin;
    private String role;
    protected boolean isActive;

    // Constructor
    public Employee(String employeeId, String name, String pin, String role) {
        this.employeeId = employeeId;
        this.name = name;
        this.pin = pin;
        this.role = role;
        this.isActive = true;
    }

    // Getters
    public String getEmployeeId() {
        return employeeId;
    }

    public String getName() {
        return name;
    }

    public String getPin() {
        return pin;
    }

    public String getRole() {
        return role;
    }

    public boolean isActive() {
        return isActive;
    }

    // Authentication method
    public boolean authenticatePin(String inputPin) {
        return this.pin != null && inputPin != null && this.pin.equals(inputPin);
    }

    // Display name for welcome message
    public String getDisplayName() {
        return name != null ? name : "Employee " + employeeId;
    }

    // Null Object Pattern (following your OnlineUser pattern)
    public static class NullEmployee extends Employee {
        private static final NullEmployee INSTANCE = new NullEmployee();

        private NullEmployee() {
            super("0000", "Unknown", "", "");
            this.isActive = false;
        }

        public static NullEmployee getInstance() {
            return INSTANCE;
        }

        @Override
        public boolean authenticatePin(String inputPin) {
            return false;
        }

        @Override
        public boolean isActive() {
            return false;
        }

        @Override
        public String getDisplayName() {
            return "Guest Employee";
        }
    }

    // Factory method for safe employee creation
    public static Employee safeEmployee(Employee employee) {
        return (employee != null && employee.isActive()) ? employee : NullEmployee.getInstance();
    }
}
