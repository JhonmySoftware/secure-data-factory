package io.github.sdf;

import io.github.sdf.generator.DeviceProfileGenerator;
import io.github.sdf.model.DeviceProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("DeviceProfileGenerator")
class DeviceProfileGeneratorTest {

    private DeviceProfileGenerator generator;

    @BeforeEach
    void setUp() {
        generator = new DeviceProfileGenerator();
    }

    @Test
    @DisplayName("generate() returns a non-null DeviceProfile")
    void generate_nonNull() {
        assertNotNull(generator.generate());
    }

    @Test
    @DisplayName("generate() populates safe device and network fields")
    void generate_populatesFields() {
        DeviceProfile deviceProfile = generator.generate();

        assertNotNull(deviceProfile.getId());
        assertNotNull(deviceProfile.getDeviceId());
        assertNotNull(deviceProfile.getPlatform());
        assertNotNull(deviceProfile.getModel());
        assertNotNull(deviceProfile.getOsVersion());
        assertNotNull(deviceProfile.getAppVersion());
        assertNotNull(deviceProfile.getHostname());
        assertNotNull(deviceProfile.getIpAddress());
        assertNotNull(deviceProfile.getMacAddress());
        assertNotNull(deviceProfile.getLocale());
        assertNotNull(deviceProfile.getEnvironment());
        assertTrue(deviceProfile.getHostname().endsWith(".example"));
        assertTrue(deviceProfile.getIpAddress().startsWith("192.0.2.")
                || deviceProfile.getIpAddress().startsWith("198.51.100.")
                || deviceProfile.getIpAddress().startsWith("203.0.113."));
        assertTrue(deviceProfile.getMacAddress().startsWith("02:"));
    }

    @Test
    @DisplayName("generate(n) returns exactly n unique device profiles")
    void generate_batchUniqueIds() {
        List<DeviceProfile> batch = generator.generate(18);
        List<String> ids = batch.stream().map(DeviceProfile::getId).distinct().collect(Collectors.toList());

        assertEquals(18, batch.size());
        assertEquals(18, ids.size());
    }
}
