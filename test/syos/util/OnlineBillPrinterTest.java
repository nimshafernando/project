package syos.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import syos.dto.ItemDTO;
import syos.model.Bill;
import syos.model.CartItem;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;

class OnlineBillPrinterTest {

    private Bill bill;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        ItemDTO item = new ItemDTO("ITEM001", "Sample Item", 250.0, 2);
        cartItem = new CartItem(item, 2);

        bill = new Bill();
        bill.setSerialNumber(100);
        bill.setDate(LocalDateTime.of(2025, 6, 6, 12, 0));
        bill.setItems(Arrays.asList(cartItem));
    }

    @Test
    void shouldPrintBillSuccessfully() throws IOException {
        try (MockedStatic<BillStorage> mocked = mockStatic(BillStorage.class)) {
            mocked.when(() -> BillStorage.getFormattedSerial(bill)).thenReturn("#100-2025-06-06");

            OnlineBillPrinter.print(bill, "cash", "user@test.com");

            File file = new File("bills/online/OnlineBill_#100-2025-06-06.txt");
            assertTrue(file.exists());

            String content = Files.readString(file.toPath());
            assertTrue(content.contains("Sample Item"));
            assertTrue(content.contains("ITEM001"));
            assertTrue(content.contains("500.00"));

            file.delete();
        }
    }

    @Test
    void shouldHandleNullUsernameGracefully() {
        try (MockedStatic<BillStorage> mocked = mockStatic(BillStorage.class)) {
            mocked.when(() -> BillStorage.getFormattedSerial(bill)).thenReturn("#100-2025-06-06");

            assertDoesNotThrow(() -> OnlineBillPrinter.print(bill, "online", null));
        }
    }

    @Test
    void shouldGenerateZeroTotalForEmptyBill() throws IOException {
        Bill emptyBill = new Bill();
        emptyBill.setSerialNumber(101);
        emptyBill.setDate(LocalDateTime.now());
        emptyBill.setItems(Collections.emptyList());

        try (MockedStatic<BillStorage> mocked = mockStatic(BillStorage.class)) {
            mocked.when(() -> BillStorage.getFormattedSerial(emptyBill)).thenReturn("#101-2025-06-06");

            OnlineBillPrinter.print(emptyBill, "wallet", "empty@bill.com");

            File file = new File("bills/online/OnlineBill_#101-2025-06-06.txt");
            assertTrue(file.exists());
            String content = Files.readString(file.toPath());
            assertTrue(content.contains("TOTAL : Rs.       0.00"));

            file.delete();
        }
    }

    @Test
    void shouldSupportLongItemNames() throws IOException {
        ItemDTO longNameItem = new ItemDTO("L001", "Very Long Product Name That Will Be Trimmed", 199.99, 1);
        CartItem longNameCartItem = new CartItem(longNameItem, 1);

        Bill longBill = new Bill();
        longBill.setSerialNumber(102);
        longBill.setDate(LocalDateTime.now());
        longBill.setItems(Arrays.asList(longNameCartItem));

        try (MockedStatic<BillStorage> mocked = mockStatic(BillStorage.class)) {
            mocked.when(() -> BillStorage.getFormattedSerial(longBill)).thenReturn("#102-2025-06-06");

            OnlineBillPrinter.print(longBill, "debit", "long@item.com");

            File file = new File("bills/online/OnlineBill_#102-2025-06-06.txt");
            assertTrue(file.exists());
            String content = Files.readString(file.toPath());
            assertTrue(content.contains("Very Long Product Name"));

            file.delete();
        }
    }

    @Test
    void shouldSupportFreeItems() throws IOException {
        ItemDTO freeItem = new ItemDTO("F001", "Free Sample", 0.00, 1);
        CartItem freeCartItem = new CartItem(freeItem, 3);

        Bill freeBill = new Bill();
        freeBill.setSerialNumber(103);
        freeBill.setDate(LocalDateTime.now());
        freeBill.setItems(Arrays.asList(freeCartItem));

        try (MockedStatic<BillStorage> mocked = mockStatic(BillStorage.class)) {
            mocked.when(() -> BillStorage.getFormattedSerial(freeBill)).thenReturn("#103-2025-06-06");

            OnlineBillPrinter.print(freeBill, "promo", "free@user.com");

            File file = new File("bills/online/OnlineBill_#103-2025-06-06.txt");
            assertTrue(file.exists());
            String content = Files.readString(file.toPath());
            assertTrue(content.contains("0.00"));

            file.delete();
        }
    }

    @Test
    void shouldCreateDirectoryIfNotExists() throws IOException {
        File onlineDir = new File("bills/online");
        if (onlineDir.exists()) {
            for (File f : onlineDir.listFiles())
                f.delete();
            onlineDir.delete();
        }

        try (MockedStatic<BillStorage> mocked = mockStatic(BillStorage.class)) {
            mocked.when(() -> BillStorage.getFormattedSerial(bill)).thenReturn("#100-2025-06-06");

            OnlineBillPrinter.print(bill, "card", "new@dir.com");

            assertTrue(onlineDir.exists());
            File file = new File("bills/online/OnlineBill_#100-2025-06-06.txt");
            assertTrue(file.exists());

            file.delete();
        }
    }

    @Test
    void shouldNotThrowIfIOExceptionOccurs() {
        File asDir = new File("bills/online/OnlineBill_#100-2025-06-06.txt");
        asDir.mkdirs();

        try (MockedStatic<BillStorage> mocked = mockStatic(BillStorage.class)) {
            mocked.when(() -> BillStorage.getFormattedSerial(bill)).thenReturn("#100-2025-06-06");

            assertDoesNotThrow(() -> OnlineBillPrinter.print(bill, "fail", "io@fail.com"));
        }

        asDir.delete();
    }

    @Test
    void shouldPrintCorrectPaymentMethodPayInStore() throws IOException {
        try (MockedStatic<BillStorage> mocked = mockStatic(BillStorage.class)) {
            mocked.when(() -> BillStorage.getFormattedSerial(bill)).thenReturn("#204-2025-06-06");

            OnlineBillPrinter.print(bill, "Pay in Store", "user@pay.com");

            File file = new File("bills/online/OnlineBill_#204-2025-06-06.txt");
            assertTrue(file.exists());
            String content = Files.readString(file.toPath());
            assertTrue(content.contains("PAY IN STORE"));
            file.delete();
        }
    }

    @Test
    void shouldPrintCorrectPaymentMethodCashOnDelivery() throws IOException {
        try (MockedStatic<BillStorage> mocked = mockStatic(BillStorage.class)) {
            mocked.when(() -> BillStorage.getFormattedSerial(bill)).thenReturn("#205-2025-06-06");

            OnlineBillPrinter.print(bill, "Cash on Delivery", "user@cod.com");

            File file = new File("bills/online/OnlineBill_#205-2025-06-06.txt");
            assertTrue(file.exists());
            String content = Files.readString(file.toPath());
            assertTrue(content.contains("CASH ON DELIVERY"));
            file.delete();
        }
    }
}
