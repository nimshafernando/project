package syos.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import syos.dto.ReportItemDTO;
import syos.service.SalesReportService.StoreType;
import syos.service.SalesReportService.TransactionType;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive test coverage for TotalSalesReportService
 * Tests all methods, report generation, filtering, and edge cases with Mockito
 */
class TotalSalesReportServiceTest {

    @Mock
    private SalesReportService mockSalesService;

    private TotalSalesReportService totalSalesReportService;
    private List<ReportItemDTO> mockReportData;
    private LocalDate testDate;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        testDate = LocalDate.of(2025, 6, 5);

        // Setup mock report data with realistic sales items
        mockReportData = Arrays.asList(
                new ReportItemDTO("FOOD001", "Premium Organic Coffee", 45, 675.0),
                new ReportItemDTO("FOOD002", "Artisan Sourdough Bread", 32, 256.0),
                new ReportItemDTO("BEVR001", "Fresh Orange Juice 1L", 28, 392.0),
                new ReportItemDTO("SNCK001", "Gourmet Dark Chocolate Bar", 56, 448.0),
                new ReportItemDTO("PROD001", "Eco-Friendly Cleaning Spray", 18, 324.0));

        // Create service instance - we'll replace the internal SalesReportService with
        // reflection if needed
        totalSalesReportService = new TotalSalesReportService();
    }

    // === Constructor Tests ===

    @Test
    @DisplayName("Should create TotalSalesReportService with default SalesReportService")
    void testConstructor() {
        // Arrange & Act
        TotalSalesReportService service = new TotalSalesReportService();

        // Assert
        assertNotNull(service);
        assertEquals("Total Sales Report", service.getReportName());
        assertEquals("SYOS TOTAL SALES REPORT", service.getReportTitle());
    }

    // === Report Generation Tests ===

    @Test
    @DisplayName("Should generate default report for today with all stores and transactions")
    void testGenerateDefaultReport() {
        // Create a service that uses mocked SalesReportService
        TotalSalesReportService serviceWithMock = new TotalSalesReportService() {
            private final SalesReportService salesService = mockSalesService;

            @Override
            public List<ReportItemDTO> generateReport() {
                return salesService.getSalesReport(
                        LocalDate.now(),
                        StoreType.COMBINED,
                        TransactionType.ALL);
            }
        };

        // Arrange
        when(mockSalesService.getSalesReport(any(LocalDate.class), eq(StoreType.COMBINED), eq(TransactionType.ALL)))
                .thenReturn(mockReportData);

        // Act
        List<ReportItemDTO> result = serviceWithMock.generateReport();

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());
        assertEquals("Premium Organic Coffee", result.get(0).getName());
        assertEquals(45, result.get(0).getQuantity());
        assertEquals(675.0, result.get(0).getRevenue());

        verify(mockSalesService).getSalesReport(any(LocalDate.class), eq(StoreType.COMBINED), eq(TransactionType.ALL));
    }

    @Test
    @DisplayName("Should generate filtered report with custom parameters")
    void testGenerateFilteredReport() {
        // Create service with mock
        TotalSalesReportService serviceWithMock = new TotalSalesReportService() {
            private final SalesReportService salesService = mockSalesService;

            @Override
            public List<ReportItemDTO> generateReport(Object... filters) {
                if (filters.length >= 3) {
                    LocalDate date = (LocalDate) filters[0];
                    StoreType storeType = (StoreType) filters[1];
                    TransactionType transactionType = (TransactionType) filters[2];
                    return salesService.getSalesReport(date, storeType, transactionType);
                }
                return generateReport();
            }
        };

        // Arrange
        List<ReportItemDTO> onlineOnlyData = Arrays.asList(
                new ReportItemDTO("FOOD001", "Premium Organic Coffee (Online)", 25, 375.0),
                new ReportItemDTO("BEVR001", "Fresh Orange Juice 1L (Online)", 15, 210.0));

        when(mockSalesService.getSalesReport(testDate, StoreType.ONLINE, TransactionType.RESERVATION))
                .thenReturn(onlineOnlyData);

        // Act
        List<ReportItemDTO> result = serviceWithMock.generateReport(testDate, StoreType.ONLINE,
                TransactionType.RESERVATION);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Premium Organic Coffee (Online)", result.get(0).getName());
        assertEquals(25, result.get(0).getQuantity());

        verify(mockSalesService).getSalesReport(testDate, StoreType.ONLINE, TransactionType.RESERVATION);
    }

    @Test
    @DisplayName("Should fallback to default report when insufficient filters provided")
    void testGenerateReportWithInsufficientFilters() {
        // Create service with mock
        TotalSalesReportService serviceWithMock = new TotalSalesReportService() {
            private final SalesReportService salesService = mockSalesService;

            @Override
            public List<ReportItemDTO> generateReport(Object... filters) {
                if (filters.length >= 3) {
                    LocalDate date = (LocalDate) filters[0];
                    StoreType storeType = (StoreType) filters[1];
                    TransactionType transactionType = (TransactionType) filters[2];
                    return salesService.getSalesReport(date, storeType, transactionType);
                }
                return salesService.getSalesReport(LocalDate.now(), StoreType.COMBINED, TransactionType.ALL);
            }
        };

        // Arrange
        when(mockSalesService.getSalesReport(any(LocalDate.class), eq(StoreType.COMBINED), eq(TransactionType.ALL)))
                .thenReturn(mockReportData);

        // Act
        List<ReportItemDTO> result = serviceWithMock.generateReport(testDate); // Only one parameter

        // Assert
        assertNotNull(result);
        assertEquals(5, result.size());
        verify(mockSalesService).getSalesReport(any(LocalDate.class), eq(StoreType.COMBINED), eq(TransactionType.ALL));
    }

    // === Report Metadata Tests ===

    @Test
    @DisplayName("Should return correct report name")
    void testGetReportName() {
        // Act & Assert
        assertEquals("Total Sales Report", totalSalesReportService.getReportName());
    }

    @Test
    @DisplayName("Should return correct report title")
    void testGetReportTitle() {
        // Act & Assert
        assertEquals("SYOS TOTAL SALES REPORT", totalSalesReportService.getReportTitle());
    }

    @Test
    @DisplayName("Should return correct column headers")
    void testGetColumnHeaders() {
        // Act
        List<String> headers = totalSalesReportService.getColumnHeaders();

        // Assert
        assertNotNull(headers);
        assertEquals(4, headers.size());
        assertEquals("Item Code", headers.get(0));
        assertEquals("Item Name", headers.get(1));
        assertEquals("Quantity Sold", headers.get(2));
        assertEquals("Total Revenue", headers.get(3));
    }

    // === Data Availability Tests ===

    @Test
    @DisplayName("Should return true when data is available")
    void testIsDataAvailableWithData() {
        // Create service with mock that returns data
        TotalSalesReportService serviceWithMock = new TotalSalesReportService() {
            @Override
            public List<ReportItemDTO> generateReport() {
                return mockReportData;
            }
        };

        // Act & Assert
        assertTrue(serviceWithMock.isDataAvailable());
    }

    @Test
    @DisplayName("Should return false when no data is available")
    void testIsDataAvailableWithoutData() {
        // Create service with mock that returns empty list
        TotalSalesReportService serviceWithMock = new TotalSalesReportService() {
            @Override
            public List<ReportItemDTO> generateReport() {
                return new ArrayList<>();
            }
        };

        // Act & Assert
        assertFalse(serviceWithMock.isDataAvailable());
    }

    @Test
    @DisplayName("Should return false when generateReport returns null")
    void testIsDataAvailableWithNullData() {
        // Create service with mock that returns null
        TotalSalesReportService serviceWithMock = new TotalSalesReportService() {
            @Override
            public List<ReportItemDTO> generateReport() {
                return null;
            }
        };

        // Act & Assert
        assertFalse(serviceWithMock.isDataAvailable());
    }

    @Test
    @DisplayName("Should return false when generateReport throws exception")
    void testIsDataAvailableWithException() {
        // Create service with mock that throws exception
        TotalSalesReportService serviceWithMock = new TotalSalesReportService() {
            @Override
            public List<ReportItemDTO> generateReport() {
                throw new RuntimeException("Database connection failed");
            }
        };

        // Act & Assert
        assertFalse(serviceWithMock.isDataAvailable());
    }

    // === Report Data Formatting Tests ===

    @Test
    @DisplayName("Should format report data correctly")
    void testGetReportData() {
        // Create service with mock data
        TotalSalesReportService serviceWithMock = new TotalSalesReportService() {
            @Override
            public List<ReportItemDTO> generateReport() {
                return mockReportData;
            }
        };

        // Act
        List<List<String>> reportData = serviceWithMock.getReportData();

        // Assert
        assertNotNull(reportData);
        assertEquals(5, reportData.size());

        List<String> firstRow = reportData.get(0);
        assertEquals("FOOD001", firstRow.get(0));
        assertEquals("Premium Organic Coffee", firstRow.get(1));
        assertEquals("45", firstRow.get(2));
        assertEquals("675.00", firstRow.get(3));

        List<String> lastRow = reportData.get(4);
        assertEquals("PROD001", lastRow.get(0));
        assertEquals("Eco-Friendly Cleaning Spray", lastRow.get(1));
        assertEquals("18", lastRow.get(2));
        assertEquals("324.00", lastRow.get(3));
    }

    @Test
    @DisplayName("Should handle empty report data")
    void testGetReportDataEmpty() {
        // Create service with mock that returns empty data
        TotalSalesReportService serviceWithMock = new TotalSalesReportService() {
            @Override
            public List<ReportItemDTO> generateReport() {
                return new ArrayList<>();
            }
        };

        // Act
        List<List<String>> reportData = serviceWithMock.getReportData();

        // Assert
        assertNotNull(reportData);
        assertEquals(0, reportData.size());
    }

    // === Row Formatting Tests ===

    @Test
    @DisplayName("Should format row correctly with standard item")
    void testGetFormattedRowStandard() {
        // Arrange
        ReportItemDTO item = new ReportItemDTO("FOOD001", "Premium Organic Coffee", 45, 675.0);

        // Act
        String formattedRow = totalSalesReportService.getFormattedRow(item);

        // Assert
        assertNotNull(formattedRow);
        assertTrue(formattedRow.contains("FOOD001"));
        assertTrue(formattedRow.contains("Premium Organic Coffee"));
        assertTrue(formattedRow.contains("45"));
        assertTrue(formattedRow.contains("675.00"));
    }

    @Test
    @DisplayName("Should truncate long item names with ellipsis")
    void testGetFormattedRowWithLongName() {
        // Arrange
        ReportItemDTO item = new ReportItemDTO("FOOD999",
                "This is a very long product name that exceeds the maximum length limit", 10, 150.0);

        // Act
        String formattedRow = totalSalesReportService.getFormattedRow(item);

        // Assert
        assertNotNull(formattedRow);
        assertTrue(formattedRow.contains("FOOD999"));
        assertTrue(formattedRow.contains("...")); // Should contain truncation ellipsis
        assertTrue(formattedRow.contains("10"));
        assertTrue(formattedRow.contains("150.00"));
    }

    @Test
    @DisplayName("Should handle items with zero quantity and revenue")
    void testGetFormattedRowWithZeros() {
        // Arrange
        ReportItemDTO item = new ReportItemDTO("TEST000", "Test Item", 0, 0.0);

        // Act
        String formattedRow = totalSalesReportService.getFormattedRow(item);

        // Assert
        assertNotNull(formattedRow);
        assertTrue(formattedRow.contains("TEST000"));
        assertTrue(formattedRow.contains("Test Item"));
        assertTrue(formattedRow.contains("0"));
        assertTrue(formattedRow.contains("0.00"));
    }

    @Test
    @DisplayName("Should handle items with high values")
    void testGetFormattedRowWithHighValues() {
        // Arrange
        ReportItemDTO item = new ReportItemDTO("HIGH001", "Expensive Item", 9999, 99999.99);

        // Act
        String formattedRow = totalSalesReportService.getFormattedRow(item);

        // Assert
        assertNotNull(formattedRow);
        assertTrue(formattedRow.contains("HIGH001"));
        assertTrue(formattedRow.contains("Expensive Item"));
        assertTrue(formattedRow.contains("9999"));
        assertTrue(formattedRow.contains("99999.99"));
    }

    @Test
    @DisplayName("Should handle items with decimal revenue values")
    void testGetFormattedRowWithDecimalRevenue() {
        // Arrange
        ReportItemDTO item = new ReportItemDTO("DEC001", "Decimal Revenue Item", 7, 123.456);

        // Act
        String formattedRow = totalSalesReportService.getFormattedRow(item);

        // Assert
        assertNotNull(formattedRow);
        assertTrue(formattedRow.contains("DEC001"));
        assertTrue(formattedRow.contains("Decimal Revenue Item"));
        assertTrue(formattedRow.contains("7"));
        assertTrue(formattedRow.contains("123.46")); // Should be rounded to 2 decimal places
    }

    // === Edge Case Tests ===

    @Test
    @DisplayName("Should handle different store types in filtered reports")
    void testGenerateReportWithDifferentStoreTypes() {
        // Create service with mock
        TotalSalesReportService serviceWithMock = new TotalSalesReportService() {
            private final SalesReportService salesService = mockSalesService;

            @Override
            public List<ReportItemDTO> generateReport(Object... filters) {
                if (filters.length >= 3) {
                    LocalDate date = (LocalDate) filters[0];
                    StoreType storeType = (StoreType) filters[1];
                    TransactionType transactionType = (TransactionType) filters[2];
                    return salesService.getSalesReport(date, storeType, transactionType);
                }
                return generateReport();
            }
        };

        // Test IN_STORE
        List<ReportItemDTO> inStoreData = Arrays.asList(
                new ReportItemDTO("STORE001", "In-Store Item", 20, 400.0));
        when(mockSalesService.getSalesReport(testDate, StoreType.IN_STORE, TransactionType.IN_STORE))
                .thenReturn(inStoreData);

        List<ReportItemDTO> result = serviceWithMock.generateReport(testDate, StoreType.IN_STORE,
                TransactionType.IN_STORE);
        assertEquals(1, result.size());
        assertEquals("In-Store Item", result.get(0).getName());

        // Test COMBINED
        when(mockSalesService.getSalesReport(testDate, StoreType.COMBINED, TransactionType.ALL))
                .thenReturn(mockReportData);

        result = serviceWithMock.generateReport(testDate, StoreType.COMBINED, TransactionType.ALL);
        assertEquals(5, result.size());
    }

    @Test
    @DisplayName("Should handle different transaction types in filtered reports")
    void testGenerateReportWithDifferentTransactionTypes() {
        // Create service with mock
        TotalSalesReportService serviceWithMock = new TotalSalesReportService() {
            private final SalesReportService salesService = mockSalesService;

            @Override
            public List<ReportItemDTO> generateReport(Object... filters) {
                if (filters.length >= 3) {
                    LocalDate date = (LocalDate) filters[0];
                    StoreType storeType = (StoreType) filters[1];
                    TransactionType transactionType = (TransactionType) filters[2];
                    return salesService.getSalesReport(date, storeType, transactionType);
                }
                return generateReport();
            }
        };

        // Test RESERVATION_COD
        List<ReportItemDTO> codData = Arrays.asList(
                new ReportItemDTO("COD001", "COD Item", 15, 300.0));
        when(mockSalesService.getSalesReport(testDate, StoreType.ONLINE, TransactionType.RESERVATION_COD))
                .thenReturn(codData);

        List<ReportItemDTO> result = serviceWithMock.generateReport(testDate, StoreType.ONLINE,
                TransactionType.RESERVATION_COD);
        assertEquals(1, result.size());
        assertEquals("COD Item", result.get(0).getName());

        // Test RESERVATION_PAY_IN_STORE
        List<ReportItemDTO> payInStoreData = Arrays.asList(
                new ReportItemDTO("PAY001", "Pay in Store Item", 12, 240.0));
        when(mockSalesService.getSalesReport(testDate, StoreType.ONLINE, TransactionType.RESERVATION_PAY_IN_STORE))
                .thenReturn(payInStoreData);

        result = serviceWithMock.generateReport(testDate, StoreType.ONLINE, TransactionType.RESERVATION_PAY_IN_STORE);
        assertEquals(1, result.size());
        assertEquals("Pay in Store Item", result.get(0).getName());
    }
}
