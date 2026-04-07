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
package io.github.sdf.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.util.Objects;

/**
 * Represents a synthetic device or client profile for QA and integration testing.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class DeviceProfile {

    private String id;
    private String deviceId;
    private String platform;
    private String model;
    private String osVersion;
    private String appVersion;
    private String hostname;
    private String ipAddress;
    private String macAddress;
    private String locale;
    private String environment;

    public DeviceProfile() {
    }

    private DeviceProfile(Builder builder) {
        this.id = builder.id;
        this.deviceId = builder.deviceId;
        this.platform = builder.platform;
        this.model = builder.model;
        this.osVersion = builder.osVersion;
        this.appVersion = builder.appVersion;
        this.hostname = builder.hostname;
        this.ipAddress = builder.ipAddress;
        this.macAddress = builder.macAddress;
        this.locale = builder.locale;
        this.environment = builder.environment;
    }

    public String getId() {
        return id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public String getPlatform() {
        return platform;
    }

    public String getModel() {
        return model;
    }

    public String getOsVersion() {
        return osVersion;
    }

    public String getAppVersion() {
        return appVersion;
    }

    public String getHostname() {
        return hostname;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public String getLocale() {
        return locale;
    }

    public String getEnvironment() {
        return environment;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public void setPlatform(String platform) {
        this.platform = platform;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public void setOsVersion(String osVersion) {
        this.osVersion = osVersion;
    }

    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    public void setHostname(String hostname) {
        this.hostname = hostname;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public void setLocale(String locale) {
        this.locale = locale;
    }

    public void setEnvironment(String environment) {
        this.environment = environment;
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String id;
        private String deviceId;
        private String platform;
        private String model;
        private String osVersion;
        private String appVersion;
        private String hostname;
        private String ipAddress;
        private String macAddress;
        private String locale;
        private String environment;

        public Builder id(String id) {
            this.id = id;
            return this;
        }

        public Builder deviceId(String deviceId) {
            this.deviceId = deviceId;
            return this;
        }

        public Builder platform(String platform) {
            this.platform = platform;
            return this;
        }

        public Builder model(String model) {
            this.model = model;
            return this;
        }

        public Builder osVersion(String osVersion) {
            this.osVersion = osVersion;
            return this;
        }

        public Builder appVersion(String appVersion) {
            this.appVersion = appVersion;
            return this;
        }

        public Builder hostname(String hostname) {
            this.hostname = hostname;
            return this;
        }

        public Builder ipAddress(String ipAddress) {
            this.ipAddress = ipAddress;
            return this;
        }

        public Builder macAddress(String macAddress) {
            this.macAddress = macAddress;
            return this;
        }

        public Builder locale(String locale) {
            this.locale = locale;
            return this;
        }

        public Builder environment(String environment) {
            this.environment = environment;
            return this;
        }

        public DeviceProfile build() {
            return new DeviceProfile(this);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) {
            return true;
        }
        if (!(other instanceof DeviceProfile)) {
            return false;
        }
        DeviceProfile that = (DeviceProfile) other;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "DeviceProfile{" +
                "id='" + id + '\'' +
                ", deviceId='" + deviceId + '\'' +
                ", platform='" + platform + '\'' +
                ", hostname='" + hostname + '\'' +
                ", ipAddress='" + ipAddress + '\'' +
                '}';
    }
}
