package syos.interfaces;

import java.util.List;

/**
 * Interface for report services - Dependency Inversion Principle
 * Defines contract for all report services with comprehensive reporting
 * capabilities
 * Implements Interface Segregation Principle with focused reporting methods
 */
public interface IReportService<T> {
    /**
     * Generate report with default parameters
     * 
     * @return List of report items
     */
    List<T> generateReport();

    /**
     * Generate report with custom filters
     * 
     * @param filters Variable arguments for filtering
     * @return List of filtered report items
     */
    List<T> generateReport(Object... filters);

    /**
     * Get the name/title of the report
     * 
     * @return Report title
     */
    String getReportName();

    /**
     * Check if data is available for this report
     * 
     * @return true if data exists, false otherwise
     */
    boolean isDataAvailable();

    /**
     * Get the title for report display
     * 
     * @return Report title for UI display
     */
    String getReportTitle();

    /**
     * Get column headers for tabular display
     * 
     * @return List of column header names
     */
    List<String> getColumnHeaders();

    /**
     * Get report data in string format for display
     * 
     * @return List of rows, each row is a list of column values
     */
    List<List<String>> getReportData();

    /**
     * Format a single report item as a row for display
     * 
     * @param item The report item to format
     * @return Formatted string representation of the item
     */
    String getFormattedRow(T item);
}
