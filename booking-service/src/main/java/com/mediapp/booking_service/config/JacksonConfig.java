package com.mediapp.booking_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.module.SimpleModule;

import java.util.UUID;

/**
 * Jackson configuration for the booking service.
 * Configures custom UUID deserializer to accept UUIDs with or without hyphens.
 */
@Configuration
public class JacksonConfig {

    @Bean
    public ObjectMapper objectMapper() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(UUID.class, new FlexibleUUIDDeserializer());

        return JsonMapper.builder()
                .addModule(module)
                .findAndAddModules()
                .build();
    }

    /**
     * Custom UUID deserializer that accepts UUIDs in both formats:
     * - Standard 36-char format: xxxxxxxx-xxxx-xxxx-xxxx-xxxxxxxxxxxx
     * - Compact 32-char format: xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx
     */
    public static class FlexibleUUIDDeserializer extends StdDeserializer<UUID> {

        public FlexibleUUIDDeserializer() {
            super(UUID.class);
        }

        @Override
        public UUID deserialize(JsonParser p, DeserializationContext ctxt) throws JacksonException {
            String uuidString = p.getText().trim();

            if (uuidString == null || uuidString.isEmpty()) {
                return null;
            }

            // If it's already in standard format (36 chars with hyphens), parse directly
            if (uuidString.length() == 36 && uuidString.contains("-")) {
                return UUID.fromString(uuidString);
            }

            // If it's in compact format (32 chars without hyphens), add hyphens
            if (uuidString.length() == 32) {
                String formatted = uuidString.substring(0, 8) + "-" +
                        uuidString.substring(8, 12) + "-" +
                        uuidString.substring(12, 16) + "-" +
                        uuidString.substring(16, 20) + "-" +
                        uuidString.substring(20);
                return UUID.fromString(formatted);
            }

            // Try parsing as-is (will throw if invalid)
            return UUID.fromString(uuidString);
        }
    }
}
