package org.networks;

import java.util.Optional;

public enum RequestType {
    GET("GET"),
    POST("POST"),
    OPTIONS("OPTIONS");

    private String name;

    RequestType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static Optional<RequestType> of(String name) {
        for (var value : RequestType.values()) {
            if (value.toString().equalsIgnoreCase(name)) {
                return Optional.of(value);
            }
        }
        return Optional.empty();
    }
}
