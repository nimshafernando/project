package syos.util;

/**
 * Constants class to eliminate magic numbers and strings
 * Contains only actively used constants
 */
public final class Constants {

    // Private constructor to prevent instantiation
    private Constants() {
        throw new AssertionError("Constants class should not be instantiated");
    }

    // Application Constants
    public static final String APPLICATION_NAME = "Synex Outlet Store";
    public static final String VERSION = "1.0.0";

    // File Paths (Used by BillStorage.java)
    public static final String BILL_SERIAL_FILE = "bill_serial.txt";
    public static final String ONLINE_BILL_SERIAL_FILE = "online_bill_serial.txt";
    public static final String BILLS_DIRECTORY = "bills/";
    public static final String STORE_BILLS_DIRECTORY = "bills/store/";
    public static final String ONLINE_BILLS_DIRECTORY = "bills/online/";

    // Database Constants (Used by DatabaseConnection.java)
    public static final String DB_URL = "jdbc:mysql://localhost:3306/syos_db";
    public static final int CONNECTION_TIMEOUT = 30;
    public static final int MAX_POOL_SIZE = 10;

    // Business Rules (Used in services)
    public static final int REORDER_LEVEL_THRESHOLD = 50;
    public static final int EXPIRY_WARNING_DAYS = 30;

    // Validation Constants (Used in POS and inventory validation)
    public static final int MIN_QUANTITY = 1;
    public static final int MAX_QUANTITY = 9999;
    public static final double MIN_PRICE = 0.01;
    public static final double MAX_PRICE = 999999.99;

    // Report Constants (Used by report services)
    public static final String REPORT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String BILL_SERIAL_FORMAT = "#%d-%s";

    // Menu Options (Used in Main.java)
    public static final int EMPLOYEE_OPTION = 1;
    public static final int CUSTOMER_OPTION = 2;
    public static final int EXIT_OPTION = 0;
}
