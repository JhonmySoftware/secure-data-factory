package io.github.sdf;

import io.github.sdf.audit.AuditEvent;
import io.github.sdf.audit.AuditListener;
import io.github.sdf.audit.AuditLogger;
import org.junit.jupiter.api.*;

import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuditLogger")
class AuditLoggerTest {

    private AuditLogger logger;

    @BeforeEach
    void setUp() {
        logger = new AuditLogger("test-service");
    }

    // ── Basic logging ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("log(event) stores the event in the buffer")
    void log_storesEvent() {
        logger.log(AuditEvent.builder(AuditEvent.EventType.DATA_GENERATED)
                .description("test event")
                .build());

        assertEquals(1, logger.getBufferSize());
    }

    @Test
    @DisplayName("log(type, description) convenience method works")
    void log_convenience() {
        logger.log(AuditEvent.EventType.DATA_ENCRYPTED, "encrypted something");
        assertEquals(1, logger.getBufferSize());
    }

    @Test
    @DisplayName("log(null) is silently ignored")
    void log_nullIsIgnored() {
        logger.log(null);
        assertEquals(0, logger.getBufferSize());
    }

    @Test
    @DisplayName("getEvents() returns all stored events in order")
    void getEvents_ordered() {
        logger.log(AuditEvent.EventType.DATA_GENERATED,  "first");
        logger.log(AuditEvent.EventType.DATA_ENCRYPTED,  "second");
        logger.log(AuditEvent.EventType.DATA_ANONYMIZED, "third");

        List<AuditEvent> events = logger.getEvents();
        assertEquals(3, events.size());
        assertEquals(AuditEvent.EventType.DATA_GENERATED,  events.get(0).getEventType());
        assertEquals(AuditEvent.EventType.DATA_ENCRYPTED,  events.get(1).getEventType());
        assertEquals(AuditEvent.EventType.DATA_ANONYMIZED, events.get(2).getEventType());
    }

    // ── Filtering ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getEventsByType() filters correctly")
    void getEventsByType_filtered() {
        logger.log(AuditEvent.EventType.DATA_GENERATED, "gen1");
        logger.log(AuditEvent.EventType.DATA_ENCRYPTED, "enc1");
        logger.log(AuditEvent.EventType.DATA_GENERATED, "gen2");

        List<AuditEvent> generated = logger.getEventsByType(AuditEvent.EventType.DATA_GENERATED);
        assertEquals(2, generated.size());
    }

    // ── Enable / disable ──────────────────────────────────────────────────────

    @Test
    @DisplayName("setEnabled(false) suppresses all new events")
    void setEnabled_false_suppressesEvents() {
        logger.setEnabled(false);
        logger.log(AuditEvent.EventType.DATA_GENERATED, "should be ignored");
        assertEquals(0, logger.getBufferSize());
    }

    @Test
    @DisplayName("re-enabling after disable allows new events")
    void setEnabled_reenabling() {
        logger.setEnabled(false);
        logger.log(AuditEvent.EventType.DATA_GENERATED, "suppressed");
        logger.setEnabled(true);
        logger.log(AuditEvent.EventType.DATA_GENERATED, "recorded");
        assertEquals(1, logger.getBufferSize());
    }

    // ── Buffer capacity ───────────────────────────────────────────────────────

    @Test
    @DisplayName("buffer drops oldest events when max capacity is reached")
    void buffer_dropsOldestWhenFull() {
        AuditLogger small = new AuditLogger("small", 5);
        for (int i = 0; i < 10; i++) {
            small.log(AuditEvent.EventType.DATA_GENERATED, "event-" + i);
        }
        assertEquals(5, small.getBufferSize());
    }

    // ── Listeners ─────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addListener() receives events when they are logged")
    void listener_receivesEvents() {
        AtomicInteger counter = new AtomicInteger(0);
        logger.addListener(event -> counter.incrementAndGet());

        logger.log(AuditEvent.EventType.DATA_GENERATED, "event 1");
        logger.log(AuditEvent.EventType.DATA_GENERATED, "event 2");

        assertEquals(2, counter.get());
    }

    @Test
    @DisplayName("removeListener() stops receiving events")
    void listener_removed() {
        AtomicInteger counter = new AtomicInteger(0);
        // AuditListener is a @FunctionalInterface in io.github.sdf.audit
        AuditListener auditListener = event -> counter.incrementAndGet();

        logger.addListener(auditListener);
        logger.removeListener(auditListener);
        logger.log(AuditEvent.EventType.DATA_GENERATED, "should not reach listener");

        assertEquals(0, counter.get());
    }

    @Test
    @DisplayName("faulty listener does not crash the logger")
    void listener_faultyListenerIsIsolated() {
        logger.addListener(event -> { throw new RuntimeException("boom"); });
        assertDoesNotThrow(() ->
                logger.log(AuditEvent.EventType.DATA_GENERATED, "safe"));
    }

    // ── Thread safety ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("concurrent logging is thread-safe")
    void log_threadSafe() throws InterruptedException {
        int threads = 10;
        int eventsPerThread = 100;
        int total = threads * eventsPerThread;
        // Use a dedicated logger with enough buffer capacity
        AuditLogger bigLogger = new AuditLogger("concurrent", total + 1);
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch latch = new CountDownLatch(threads);

        for (int t = 0; t < threads; t++) {
            executor.submit(() -> {
                for (int i = 0; i < eventsPerThread; i++) {
                    bigLogger.log(AuditEvent.EventType.DATA_GENERATED, "concurrent");
                }
                latch.countDown();
            });
        }

        assertTrue(latch.await(10, TimeUnit.SECONDS));
        executor.shutdown();
        assertEquals(total, bigLogger.getBufferSize());
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    @Test
    @DisplayName("clearBuffer() empties the event store")
    void clearBuffer() {
        logger.log(AuditEvent.EventType.DATA_GENERATED, "event");
        logger.clearBuffer();
        assertEquals(0, logger.getBufferSize());
    }

    @Test
    @DisplayName("getServiceId() returns the service ID provided at construction")
    void getServiceId() {
        assertEquals("test-service", logger.getServiceId());
    }
}
