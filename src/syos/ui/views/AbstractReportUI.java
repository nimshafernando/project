package syos.ui.views;

import syos.interfaces.IReportService;
import syos.util.ConsoleUtils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

/**
 * Enhanced Template Method pattern for all report UIs
 * Provides comprehensive reporting workflow with hooks for customization
 * Implements SOLID principles: SRP (report display workflow), OCP (extensible
 * via hooks),
 * LSP (concrete reports are substitutable), ISP (specific report interfaces),
 * DIP (depends on report service abstractions)
 */
public abstract class AbstractReportUI<T> {

    protected final Scanner scanner;
    protected final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    // Template method execution context
    protected String reportTitle;
    protected LocalDateTime executionStartTime;
    protected LocalDateTime executionEndTime;
    protected Exception lastError;
    protected int recordCount;

    protected AbstractReportUI(Scanner scanner) {
        this.scanner = scanner;
        this.reportTitle = getReportTitle();
    }

    /**
     * Enhanced Template Method - main display workflow
     * Returns early if user chooses to exit during criteria gathering
     */
    public final void display() {
        try {
            clearScreen();

            // Give user option to exit during criteria gathering
            if (!gatherReportCriteria()) {
                System.out.println("Returning to reports menu...");
                waitForEnter();
                return;
            }

            List<T> items = fetchReportItems();

            if (items == null || items.isEmpty()) {
                handleEmptyResults();
                showNoDataMessage();
            } else {
                renderReport(items);
                waitForEnter();
            }

        } catch (Exception e) {
            handleFetchError(e);
            showError(e);
        }
    }

    // === Abstract Methods (must be implemented by subclasses) ===

    /**
     * Get the report title for display
     */
    protected abstract String getReportTitle();

    /**
     * Gather report criteria from user input
     * 
     * @return true if criteria gathered successfully, false to cancel
     */
    protected abstract boolean gatherReportCriteria();

    /**
     * Fetch report data based on gathered criteria
     */
    protected abstract List<T> fetchReportItems() throws Exception;

    /**
     * Render the main report content
     */
    protected abstract void renderReport(List<T> items);

    // === Hook Methods (can be overridden by subclasses) ===

    /**
     * Initialize report-specific state
     */
    protected void initializeReport() {
        lastError = null;
        recordCount = 0;
    }

    /**
     * Called before report execution begins
     */
    protected void beforeReportExecution() {
        // Default: no action
    }

    /**
     * Called after successful report execution
     */
    protected void afterReportExecution(List<T> reportData) {
        // Default: no action
    }

    /**
     * Validate gathered criteria
     */
    protected boolean validateCriteria() {
        return true; // Default: always valid
    }

    /**
     * Process/transform report data before rendering
     */
    protected List<T> processReportData(List<T> rawData) {
        return rawData; // Default: no processing
    }

    /**
     * Validate report results
     */
    protected boolean validateResults(List<T> reportData) {
        return reportData != null && !reportData.isEmpty();
    }

    /**
     * Offer export options to user
     */
    protected void offerExportOptions(List<T> reportData) {
        // Default: no export options
    }

    /**
     * Cleanup resources after report execution
     */
    protected void cleanupReport() {
        // Default: no cleanup needed
    }

    // === Display Methods ===

    /**
     * Clear screen using utility
     */
    protected void clearScreen() {
        ConsoleUtils.clearScreen();
    }

    /**
     * Display report header with title and timestamp
     */
    protected void displayReportHeader() {
        System.out.println("========================================");
        System.out.println("  " + reportTitle);
        System.out.println("  Generated: " + LocalDateTime.now().format(dateFormatter));
        System.out.println("========================================");
    }

    /**
     * Display report data header
     */
    protected void renderReportHeader(List<T> reportData) {
        System.out.println("\n--- Report Results ---");
        System.out.println("Total Records: " + recordCount);
        if (recordCount > 0) {
            System.out.println();
        }
    }

    /**
     * Display report footer with execution stats
     */
    protected void renderReportFooter(List<T> reportData) {
        System.out.println("\n--- End of Report ---");
        if (executionStartTime != null && executionEndTime != null) {
            long executionMs = java.time.Duration.between(executionStartTime, executionEndTime).toMillis();
            System.out.println("Execution time: " + executionMs + "ms");
        }
    }

    /**
     * Show processing message
     */
    protected void showProcessingMessage() {
        System.out.println("\nGenerating report, please wait...");
    }

    // === Error Handling Methods ===

    /**
     * Handle user cancellation
     */
    protected void handleCancellation() {
        System.out.println("Report generation cancelled.");
        pauseForUser();
    }

    /**
     * Handle criteria validation failure
     */
    protected void handleValidationFailure() {
        System.out.println("Invalid criteria provided. Please try again.");
        pauseForUser();
    }

    /**
     * Handle data fetch errors
     */
    protected void handleFetchError(Exception e) {
        lastError = e;
        System.err.println("Error fetching report data: " + e.getMessage());
        System.out.println("Please try again or contact system administrator.");
        pauseForUser();
    }

    /**
     * Handle rendering errors
     */
    protected void handleRenderError(Exception e) {
        lastError = e;
        System.err.println("Error rendering report: " + e.getMessage());
        pauseForUser();
    }

    /**
     * Handle empty results
     */
    protected void handleEmptyResults() {
        System.out.println("\nNo data found matching your criteria.");
        System.out.println("Try adjusting your search parameters.");
    }

    /**
     * Enhanced error handling with user-friendly messages
     */
    protected void showError(Exception e) {
        System.out.println("\n[Error] " + e.getMessage());
        System.out.print("Press Enter to return to reports menu...");
        scanner.nextLine();
    }

    /**
     * Enhanced no data message
     */
    protected void showNoDataMessage() {
        System.out.println("\n[Info] No records found for the selected criteria.");
        System.out.println("Try selecting a different date or criteria.");
        waitForEnter();
    }

    /**
     * Wait for user input with clear message
     */
    protected void waitForEnter() {
        System.out.print("\nPress Enter to continue...");
        scanner.nextLine();
    }

    // === Utility Methods ===

    /**
     * Prompt user to repeat report generation
     */
    protected boolean promptRepeat() {
        System.out.print("\nGenerate another report? (y/n): ");
        String response = scanner.nextLine().trim().toLowerCase();
        return response.equals("y") || response.equals("yes");
    }

    /**
     * Pause for user input
     */
    protected void pauseForUser() {
        System.out.print("Press Enter to continue...");
        scanner.nextLine();
    }

    /**
     * Get formatted current timestamp
     */
    protected String getCurrentTimestamp() {
        return LocalDateTime.now().format(dateFormatter);
    }

    /**
     * Get execution statistics
     */
    protected String getExecutionStats() {
        if (executionStartTime == null) {
            return "No execution data available";
        }

        LocalDateTime endTime = executionEndTime != null ? executionEndTime : LocalDateTime.now();
        long durationMs = java.time.Duration.between(executionStartTime, endTime).toMillis();

        return String.format("Records: %d, Duration: %dms, Status: %s",
                recordCount,
                durationMs,
                lastError == null ? "Success" : "Error");
    }

    /**
     * Format number with commas for display
     */
    protected String formatNumber(Number number) {
        return String.format("%,d", number.longValue());
    }

    /**
     * Format currency for display
     */
    protected String formatCurrency(double amount) {
        return String.format("Rs. %.2f", amount);
    }

    /**
     * Check if report execution was successful
     */
    protected boolean wasSuccessful() {
        return lastError == null && recordCount >= 0;
    }

    // === Factory Method for Report Service Integration ===

    /**
     * Get report service for this UI (if applicable)
     * Subclasses can override to provide specific service
     */
    protected IReportService<T> getReportService() {
        return null; // Default: no service
    }

    /**
     * Execute report using service if available
     */
    protected List<T> executeReportService() throws Exception {
        IReportService<T> service = getReportService();
        if (service != null) {
            return service.generateReport();
        }
        throw new UnsupportedOperationException("No report service available");
    }
}
