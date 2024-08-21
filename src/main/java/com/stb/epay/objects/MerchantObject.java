package com.stb.epay.objects;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.Map;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MerchantObject implements Serializable {
    private int id;
    private String merchantName;
    private int merchantType;
    private String email;
    private String mobile;
    private String username;
    private String password;
    private boolean isEncryption;
    private boolean isSession;
    private String key;
    private String publicKey;

    private String prefixRefNo;
    private boolean status;
    private Map<String, ServiceObject> services;
    private Map<String, FunctionObject> functions;
    private Map<String, PropertyObject> properties;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ServiceObject implements Serializable {
        private int id;
        private int merchantId;
        private String serviceName;
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FunctionObject implements Serializable {
        private int id;
        private int merchantId;
        private String functionName;
        private double fee;
        private boolean isPercentage;
        private int percentage;
        private double minFee;
        private double maxFee;
        private Integer timeout;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder

    public static class PropertyObject implements Serializable {
        private int id;
        private int merchantId;
        private String propertyName;
        private String propertyValue;
    }
}
