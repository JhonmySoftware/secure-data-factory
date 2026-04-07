package io.github.sdf;

import io.github.sdf.crypto.SecurityLevel;
import io.github.sdf.model.Person;
import io.github.sdf.processor.BatchProcessor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BatchProcessor")
class BatchProcessorTest {

    private SecureDataFactory factory;
    private BatchProcessor batchProcessor;

    @BeforeEach
    void setUp() {
        factory = SecureDataFactory.builder()
                .securityLevel(SecurityLevel.MEDIUM)
                .enableAudit(false)
                .build();
        batchProcessor = new BatchProcessor(factory, 2);
    }

    @AfterEach
    void tearDown() {
        batchProcessor.shutdown();
    }

    @Test
    @DisplayName("generatePersonsParallel returns correct count")
    void generatePersonsParallel_returnsCorrectCount() {
        int targetCount = 100;
        List<SecureData<Person>> results = batchProcessor.generatePersonsParallel(targetCount);
        assertEquals(targetCount, results.size());
    }

    @Test
    @DisplayName("generatePersonsParallel produces unique IDs")
    void generatePersonsParallel_producesUniqueIds() {
        int targetCount = 100;
        List<SecureData<Person>> results = batchProcessor.generatePersonsParallel(targetCount);
        
        long uniqueIds = results.stream()
                .map(sd -> sd.getData().getId())
                .distinct()
                .count();
        assertEquals(targetCount, uniqueIds);
    }

    @Test
    @DisplayName("generatePersonsParallel returns unmodifiable list")
    void generatePersonsParallel_returnsUnmodifiableList() {
        List<SecureData<Person>> results = batchProcessor.generatePersonsParallel(10);
        assertThrows(UnsupportedOperationException.class, () -> results.add(null));
    }

    @Test
    @DisplayName("generatePersonsParallelWithProgress tracks progress")
    void generatePersonsParallelWithProgress_tracksProgress() {
        int targetCount = 50;
        AtomicInteger lastProgress = new AtomicInteger(0);
        
        List<SecureData<Person>> results = batchProcessor.generatePersonsParallelWithProgress(
                targetCount,
                progress -> lastProgress.set(progress)
        );
        
        assertEquals(targetCount, results.size());
        assertEquals(targetCount, lastProgress.get());
    }

    @Test
    @DisplayName("measureThroughput returns valid metrics")
    void measureThroughput_returnsValidMetrics() {
        BatchProcessor.BatchMetrics metrics = batchProcessor.measureThroughput(
                100,
                () -> factory.generatePersons(100)
        );
        
        assertEquals(100, metrics.recordCount);
        assertTrue(metrics.elapsedMillis >= 0);
        assertTrue(metrics.recordsPerSecond > 0);
        assertEquals(2, metrics.threadCount);
    }

    @Test
    @DisplayName("BatchGenerationReport calculates total records correctly")
    void batchGenerationReport_calculatesTotalRecords() {
        BatchProcessor.DatasetConfig config = BatchProcessor.DatasetConfig.defaults()
                .persons(10)
                .companies(5)
                .products(20)
                .addresses(10)
                .bankAccounts(5)
                .devices(10)
                .invoices(5)
                .orders(5)
                .withInvoiceReferences(true)
                .withOrderReferences(true);

        BatchProcessor.BatchGenerationReport report = batchProcessor.generateComprehensiveDataset(config);

        assertEquals(10 + 5 + 20 + 10 + 5 + 10 + 5 + 5, report.totalRecords);
        assertEquals(10, report.persons.size());
        assertEquals(5, report.companies.size());
        assertEquals(20, report.products.size());
        assertEquals(10, report.addresses.size());
        assertEquals(5, report.bankAccounts.size());
        assertEquals(10, report.deviceProfiles.size());
        assertEquals(5, report.invoiceReferences.size());
        assertEquals(5, report.orderReferences.size());
    }

    @Test
    @DisplayName("BatchGenerationReport calculates throughput correctly")
    void batchGenerationReport_calculatesThroughput() {
        BatchProcessor.DatasetConfig config = BatchProcessor.DatasetConfig.defaults()
                .persons(50)
                .withInvoiceReferences(false)
                .withOrderReferences(false);

        BatchProcessor.BatchGenerationReport report = batchProcessor.generateComprehensiveDataset(config);

        assertTrue(report.getRecordsPerSecond() > 0);
    }

    @Test
    @DisplayName("DatasetConfig builder patterns work correctly")
    void datasetConfig_builderPatterns() {
        BatchProcessor.DatasetConfig config = new BatchProcessor.DatasetConfig()
                .persons(100)
                .companies(50)
                .products(200)
                .addresses(100)
                .bankAccounts(50)
                .devices(100)
                .invoices(25)
                .orders(25)
                .withInvoiceReferences(true)
                .withOrderReferences(true);

        assertEquals(100, config.personCount);
        assertEquals(50, config.companyCount);
        assertEquals(200, config.productCount);
        assertEquals(100, config.addressCount);
        assertEquals(50, config.bankAccountCount);
        assertEquals(100, config.deviceCount);
        assertEquals(25, config.invoiceCount);
        assertEquals(25, config.orderCount);
        assertTrue(config.generateInvoiceReferences);
        assertTrue(config.generateOrderReferences);
    }

    @Test
    @DisplayName("BatchMetrics toString returns formatted string")
    void batchMetrics_toString() {
        BatchProcessor.BatchMetrics metrics = new BatchProcessor.BatchMetrics(
                100, 500, 200.0, 4
        );
        
        String result = metrics.toString();
        assertTrue(result.contains("100"));
        assertTrue(result.contains("500"));
        assertTrue(result.contains("200"));
        assertTrue(result.contains("4"));
    }

    @Test
    @DisplayName("getThreadCount returns configured value")
    void getThreadCount_returnsConfiguredValue() {
        assertEquals(2, batchProcessor.getThreadCount());
    }

    @Test
    @DisplayName("shutdown terminates executor gracefully")
    void shutdown_terminatesGracefully() throws InterruptedException {
        batchProcessor.shutdown();
        assertTrue(batchProcessor.getThreadCount() > 0);
    }

    @Test
    @DisplayName("shutdownNow interrupts running tasks")
    void shutdownNow_interruptsRunningTasks() {
        batchProcessor.shutdownNow();
        assertTrue(batchProcessor.getThreadCount() > 0);
    }

    @Test
    @DisplayName("BatchProcessor with default constructor uses available processors")
    void batchProcessor_defaultConstructor_usesAvailableProcessors() {
        BatchProcessor defaultProcessor = new BatchProcessor(factory);
        try {
            int availableProcessors = Runtime.getRuntime().availableProcessors();
            assertEquals(availableProcessors, defaultProcessor.getThreadCount());
        } finally {
            defaultProcessor.shutdown();
        }
    }
}
