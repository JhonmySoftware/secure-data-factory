/*
 * Copyright (c) 2024 Secure Data Factory Contributors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.github.sdf.generator;

import io.github.sdf.model.DeviceProfile;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.UUID;

/**
 * Generates synthetic client/device profiles for authentication, telemetry, and QA flows.
 */
public class DeviceProfileGenerator implements DataGenerator<DeviceProfile> {

    private static final List<String> PLATFORMS = Arrays.asList(
            "ANDROID", "IOS", "WEB", "WINDOWS", "MACOS", "LINUX"
    );
    private static final List<String> ENVIRONMENTS = Arrays.asList(
            "DEV", "QA", "STAGING", "UAT"
    );
    private static final List<String> RESERVED_NETWORKS = Arrays.asList(
            "192.0.2.", "198.51.100.", "203.0.113."
    );

    private final Locale locale;
    private final Random random;

    public DeviceProfileGenerator() {
        this(Locale.ENGLISH);
    }

    public DeviceProfileGenerator(Locale locale) {
        this.locale = locale == null ? Locale.ENGLISH : locale;
        this.random = new Random();
    }

    @Override
    public DeviceProfile generate() {
        String platform = pick(PLATFORMS);
        String environment = pick(ENVIRONMENTS);
        String suffix = randomDigits(4);

        return DeviceProfile.builder()
                .id(UUID.randomUUID().toString())
                .deviceId("DEV-" + randomAlphaNumeric(10))
                .platform(platform)
                .model(resolveModel(platform))
                .osVersion(resolveOsVersion(platform))
                .appVersion(random.nextInt(5) + "." + random.nextInt(10) + "." + random.nextInt(10))
                .hostname(environment.toLowerCase() + "-" + platform.toLowerCase() + "-" + suffix + ".example")
                .ipAddress(pick(RESERVED_NETWORKS) + (1 + random.nextInt(254)))
                .macAddress(generateMacAddress())
                .locale(locale.toLanguageTag())
                .environment(environment)
                .build();
    }

    @Override
    public String getGeneratorName() {
        return "DeviceProfileGenerator";
    }

    private String resolveModel(String platform) {
        if ("ANDROID".equals(platform)) {
            return pick(Arrays.asList("Sandbox Phone", "Mock Tablet", "Sample Fold"));
        }
        if ("IOS".equals(platform)) {
            return pick(Arrays.asList("Pilot Phone", "QA Pad", "Dev Mobile"));
        }
        if ("WEB".equals(platform)) {
            return pick(Arrays.asList("Test Browser", "Sandbox Portal", "Mock Console"));
        }
        if ("WINDOWS".equals(platform)) {
            return pick(Arrays.asList("DevStation", "QA Desktop", "Mock Workbench"));
        }
        if ("MACOS".equals(platform)) {
            return pick(Arrays.asList("SandboxBook", "Pilot Studio", "Mock Desk"));
        }
        return pick(Arrays.asList("Sample Node", "QA Host", "Dev Runner"));
    }

    private String resolveOsVersion(String platform) {
        if ("ANDROID".equals(platform)) {
            return "Android " + (11 + random.nextInt(4));
        }
        if ("IOS".equals(platform)) {
            return "iOS " + (15 + random.nextInt(3)) + "." + random.nextInt(6);
        }
        if ("WEB".equals(platform)) {
            return "Browser " + (110 + random.nextInt(20));
        }
        if ("WINDOWS".equals(platform)) {
            return "Windows 1" + random.nextInt(2);
        }
        if ("MACOS".equals(platform)) {
            return "macOS 1" + (2 + random.nextInt(3)) + "." + random.nextInt(6);
        }
        return "Linux " + (4 + random.nextInt(3)) + "." + random.nextInt(10);
    }

    private String generateMacAddress() {
        StringBuilder builder = new StringBuilder("02");
        for (int i = 0; i < 5; i++) {
            builder.append(':');
            builder.append(hexByte());
        }
        return builder.toString();
    }

    private String hexByte() {
        String value = Integer.toHexString(random.nextInt(256)).toUpperCase();
        return value.length() == 1 ? "0" + value : value;
    }

    private String randomDigits(int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            builder.append(random.nextInt(10));
        }
        return builder.toString();
    }

    private String randomAlphaNumeric(int count) {
        StringBuilder builder = new StringBuilder(count);
        for (int i = 0; i < count; i++) {
            if (random.nextBoolean()) {
                builder.append((char) ('A' + random.nextInt(26)));
            } else {
                builder.append(random.nextInt(10));
            }
        }
        return builder.toString();
    }

    private String pick(List<String> values) {
        return values.get(random.nextInt(values.size()));
    }
}
