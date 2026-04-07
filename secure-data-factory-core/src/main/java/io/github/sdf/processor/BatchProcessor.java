package io.github.sdf.processor;

import io.github.sdf.SecureData;
import io.github.sdf.SecureDataFactory;
import io.github.sdf.audit.AuditEvent;
import io.github.sdf.crypto.SecurityLevel;
import io.github.sdf.generator.DocumentGenerator;
import io.github.sdf.model.Address;
import io.github.sdf.model.BankAccount;
import io.github.sdf.model.Company;
import io.github.sdf.model.DeviceProfile;
import io.github.sdf.model.Person;
import io.github.sdf.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class BatchProcessor {

    private static final Logger log = LoggerFactory.getLogger(BatchProcessor.class);

    private final SecureDataFactory factory;
    private final int threadCount;
    private final ExecutorService executorService;

    public BatchProcessor(SecureDataFactory factory) {
        this(factory, Runtime.getRuntime().availableProcessors());
    }

    public BatchProcessor(SecureDataFactory factory, int threadCount) {
        this.factory = factory;
        this.threadCount = threadCount;
        this.executorService = Executors.newFixedThreadPool(threadCount);
    }

    public List<SecureData<Person>> generatePersonsParallel(int totalCount) {
        log.info("Starting parallel generation of {} persons using {} threads", totalCount, threadCount);
        long start = System.currentTimeMillis();

        int batchSize = calculateBatchSize(totalCount);
        List<CompletableFuture<List<SecureData<Person>>>> futures = new ArrayList<>();

        for (int i = 0; i < totalCount; i += batchSize) {
            final int remaining = Math.min(batchSize, totalCount - i);
            futures.add(CompletableFuture.supplyAsync(
                    () -> factory.generatePersons(remaining),
                    executorService
            ));
        }

        List<SecureData<Person>> results = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        long elapsed = System.currentTimeMillis() - start;
        log.info("Parallel generation complete: {} persons in {}ms ({} records/sec)",
                results.size(), elapsed, (results.size() * 1000L) / elapsed);

        return Collections.unmodifiableList(results);
    }

    public List<SecureData<Person>> generatePersonsParallelWithProgress(int totalCount, 
            java.util.function.Consumer<Integer> progressCallback) {
        log.info("Starting parallel generation with progress for {} persons", totalCount);
        long start = System.currentTimeMillis();

        int batchSize = calculateBatchSize(totalCount);
        List<CompletableFuture<List<SecureData<Person>>>> futures = new ArrayList<>();
        int[] completed = {0};

        for (int i = 0; i < totalCount; i += batchSize) {
            final int remaining = Math.min(batchSize, totalCount - i);
            final int startIndex = i;
            futures.add(CompletableFuture.supplyAsync(() -> {
                List<SecureData<Person>> batch = factory.generatePersons(remaining);
                synchronized (completed) {
                    completed[0] += remaining;
                    if (progressCallback != null) {
                        progressCallback.accept(completed[0]);
                    }
                }
                return batch;
            }, executorService));
        }

        List<SecureData<Person>> results = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        long elapsed = System.currentTimeMillis() - start;
        log.info("Parallel generation with progress complete: {} persons in {}ms", results.size(), elapsed);

        return Collections.unmodifiableList(results);
    }

    public <T> List<SecureData<T>> generateParallel(
            int totalCount,
            Function<SecureDataFactory, List<SecureData<T>>> generator,
            String typeName
    ) {
        log.info("Starting parallel generation of {} {} using {} threads", totalCount, typeName, threadCount);
        long start = System.currentTimeMillis();

        int batchSize = calculateBatchSize(totalCount);
        List<CompletableFuture<List<SecureData<T>>>> futures = new ArrayList<>();

        for (int i = 0; i < totalCount; i += batchSize) {
            final int remaining = Math.min(batchSize, totalCount - i);
            futures.add(CompletableFuture.supplyAsync(() -> {
                SecureDataFactory threadFactory = createThreadLocalFactory();
                return generator.apply(threadFactory);
            }, executorService));
        }

        List<SecureData<T>> results = futures.stream()
                .map(CompletableFuture::join)
                .flatMap(List::stream)
                .collect(Collectors.toList());

        long elapsed = System.currentTimeMillis() - start;
        log.info("Parallel generation complete: {} {} in {}ms ({} records/sec)",
                results.size(), typeName, elapsed, (results.size() * 1000L) / elapsed);

        return Collections.unmodifiableList(results);
    }

    public BatchGenerationReport generateComprehensiveDataset(DatasetConfig config) {
        log.info("Starting comprehensive dataset generation");
        long start = System.currentTimeMillis();

        BatchGenerationReport report = new BatchGenerationReport();

        CompletableFuture<List<SecureData<Person>>> personsFuture = 
                CompletableFuture.supplyAsync(() -> generatePersonsParallel(config.personCount), executorService);
        
        CompletableFuture<List<SecureData<Company>>> companiesFuture = 
                CompletableFuture.supplyAsync(() -> factory.generateCompanies(config.companyCount), executorService);
        
        CompletableFuture<List<SecureData<Product>>> productsFuture = 
                CompletableFuture.supplyAsync(() -> factory.generateProducts(config.productCount), executorService);
        
        CompletableFuture<List<SecureData<Address>>> addressesFuture = 
                CompletableFuture.supplyAsync(() -> factory.generateAddresses(config.addressCount), executorService);
        
        CompletableFuture<List<SecureData<BankAccount>>> bankAccountsFuture = 
                CompletableFuture.supplyAsync(() -> factory.generateBankAccounts(config.bankAccountCount), executorService);
        
        CompletableFuture<List<SecureData<DeviceProfile>>> devicesFuture = 
                CompletableFuture.supplyAsync(() -> factory.generateDeviceProfiles(config.deviceCount), executorService);

        report.persons = personsFuture.join();
        report.companies = companiesFuture.join();
        report.products = productsFuture.join();
        report.addresses = addressesFuture.join();
        report.bankAccounts = bankAccountsFuture.join();
        report.deviceProfiles = devicesFuture.join();

        if (config.generateInvoiceReferences) {
            report.invoiceReferences = factory.generateDocumentReferences(
                    DocumentGenerator.DocumentType.INVOICE, config.invoiceCount);
        }
        if (config.generateOrderReferences) {
            report.orderReferences = factory.generateDocumentReferences(
                    DocumentGenerator.DocumentType.ORDER, config.orderCount);
        }

        report.elapsedMillis = System.currentTimeMillis() - start;
        report.totalRecords = report.persons.size() + report.companies.size() + 
                report.products.size() + report.addresses.size() + 
                report.bankAccounts.size() + report.deviceProfiles.size() +
                (report.invoiceReferences != null ? report.invoiceReferences.size() : 0) +
                (report.orderReferences != null ? report.orderReferences.size() : 0);

        log.info("Comprehensive dataset generation complete: {} total records in {}ms",
                report.totalRecords, report.elapsedMillis);

        return report;
    }

    public BatchMetrics measureThroughput(int recordCount, 
            java.util.function.Supplier<List<SecureData<Person>>> generator) {
        long start = System.currentTimeMillis();
        List<SecureData<Person>> results = generator.get();
        long elapsed = System.currentTimeMillis() - start;

        return new BatchMetrics(
                results.size(),
                elapsed,
                (results.size() * 1000L) / Math.max(elapsed, 1),
                threadCount
        );
    }

    public void shutdown() {
        executorService.shutdown();
        try {
            if (!executorService.awaitTermination(60, TimeUnit.SECONDS)) {
                executorService.shutdownNow();
            }
        } catch (InterruptedException e) {
            executorService.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }

    public void shutdownNow() {
        executorService.shutdownNow();
    }

    private int calculateBatchSize(int totalCount) {
        int idealBatch = Math.max(1, totalCount / (threadCount * 4));
        return Math.min(idealBatch, 500);
    }

    private SecureDataFactory createThreadLocalFactory() {
        return SecureDataFactory.builder()
                .securityLevel(factory.getSecurityLevel())
                .enableAudit(false)
                .applyPolicies(false)
                .build();
    }

    public int getThreadCount() {
        return threadCount;
    }

    public static class DatasetConfig {
        public int personCount = 100;
        public int companyCount = 50;
        public int productCount = 200;
        public int addressCount = 100;
        public int bankAccountCount = 50;
        public int deviceCount = 100;
        public int invoiceCount = 100;
        public int orderCount = 100;
        public boolean generateInvoiceReferences = true;
        public boolean generateOrderReferences = true;

        public static DatasetConfig defaults() {
            return new DatasetConfig();
        }

        public DatasetConfig persons(int count) { this.personCount = count; return this; }
        public DatasetConfig companies(int count) { this.companyCount = count; return this; }
        public DatasetConfig products(int count) { this.productCount = count; return this; }
        public DatasetConfig addresses(int count) { this.addressCount = count; return this; }
        public DatasetConfig bankAccounts(int count) { this.bankAccountCount = count; return this; }
        public DatasetConfig devices(int count) { this.deviceCount = count; return this; }
        public DatasetConfig invoices(int count) { this.invoiceCount = count; return this; }
        public DatasetConfig orders(int count) { this.orderCount = count; return this; }
        public DatasetConfig withInvoiceReferences(boolean value) { this.generateInvoiceReferences = value; return this; }
        public DatasetConfig withOrderReferences(boolean value) { this.generateOrderReferences = value; return this; }
    }

    public static class BatchGenerationReport {
        public List<SecureData<Person>> persons = Collections.emptyList();
        public List<SecureData<Company>> companies = Collections.emptyList();
        public List<SecureData<Product>> products = Collections.emptyList();
        public List<SecureData<Address>> addresses = Collections.emptyList();
        public List<SecureData<BankAccount>> bankAccounts = Collections.emptyList();
        public List<SecureData<DeviceProfile>> deviceProfiles = Collections.emptyList();
        public List<SecureData<String>> invoiceReferences = Collections.emptyList();
        public List<SecureData<String>> orderReferences = Collections.emptyList();
        public long elapsedMillis = 0;
        public int totalRecords = 0;

        public double getRecordsPerSecond() {
            return elapsedMillis > 0 ? (totalRecords * 1000.0) / elapsedMillis : 0;
        }
    }

    public static class BatchMetrics {
        public final int recordCount;
        public final long elapsedMillis;
        public final double recordsPerSecond;
        public final int threadCount;

        public BatchMetrics(int recordCount, long elapsedMillis, double recordsPerSecond, int threadCount) {
            this.recordCount = recordCount;
            this.elapsedMillis = elapsedMillis;
            this.recordsPerSecond = recordsPerSecond;
            this.threadCount = threadCount;
        }

        @Override
        public String toString() {
            return String.format("BatchMetrics{records=%d, elapsed=%dms, throughput=%.2f rec/s, threads=%d}",
                    recordCount, elapsedMillis, recordsPerSecond, threadCount);
        }
    }
}
