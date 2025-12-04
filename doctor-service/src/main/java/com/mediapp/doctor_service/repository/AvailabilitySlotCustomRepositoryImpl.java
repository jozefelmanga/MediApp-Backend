package com.mediapp.doctor_service.repository;

import java.nio.ByteBuffer;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.stereotype.Repository;

import com.mediapp.doctor_service.domain.AvailabilitySlotEntity;

import io.r2dbc.spi.Row;
import io.r2dbc.spi.RowMetadata;
import reactor.core.publisher.Mono;

/**
 * Low level SQL operations that are more complex than what derived queries
 * support.
 */
@Repository
public class AvailabilitySlotCustomRepositoryImpl implements AvailabilitySlotCustomRepository {

    private final DatabaseClient databaseClient;

    public AvailabilitySlotCustomRepositoryImpl(DatabaseClient databaseClient) {
        this.databaseClient = databaseClient;
    }

    @Override
    public Mono<Boolean> existsOverlappingSlot(UUID doctorId, Instant startTime, Instant endTime) {
        String sql = """
                SELECT EXISTS (
                    SELECT 1
                    FROM availability_slot
                    WHERE doctor_id = :doctorId
                      AND start_time < :endTime
                      AND end_time > :startTime
                ) AS overlap
                """;

        return databaseClient.sql(sql)
                .bind("doctorId", doctorId)
                .bind("startTime", startTime)
                .bind("endTime", endTime)
                .map((row, metadata) -> isTruthy(row.get("overlap")))
                .one()
                .defaultIfEmpty(Boolean.FALSE);
    }

    @Override
    public Mono<AvailabilitySlotEntity> reserveSlot(UUID slotId, String reservationToken, Instant reservedAt) {
        String updateSql = """
                UPDATE availability_slot
                   SET is_reserved = TRUE,
                       reservation_token = :reservationToken,
                       reserved_at = CASE
                           WHEN reservation_token <=> :reservationToken THEN COALESCE(reserved_at, :reservedAt)
                           ELSE :reservedAt
                       END,
                       updated_at = :reservedAt,
                       version = version + CASE
                           WHEN reservation_token <=> :reservationToken THEN 0 ELSE 1
                       END
                 WHERE slot_id = :slotId
                   AND (is_reserved = FALSE OR reservation_token <=> :reservationToken)
                """;

        return databaseClient.sql(updateSql)
                .bind("reservationToken", reservationToken)
                .bind("reservedAt", reservedAt)
                .bind("slotId", slotId)
                .fetch()
                .rowsUpdated()
                .flatMap(count -> count != null && count > 0 ? fetchSlot(slotId) : Mono.empty());
    }

    private Mono<AvailabilitySlotEntity> fetchSlot(UUID slotId) {
        String selectSql = """
                SELECT slot_id, doctor_id, start_time, end_time, is_reserved, reservation_token, reserved_at, version,
                       created_at, updated_at
                  FROM availability_slot
                 WHERE slot_id = :slotId
                """;

        return databaseClient.sql(selectSql)
                .bind("slotId", slotId)
                .map(this::mapRowToSlot)
                .one();
    }

    private AvailabilitySlotEntity mapRowToSlot(Row row, RowMetadata metadata) {
        return AvailabilitySlotEntity.builder()
                .id(readUuid(row.get("slot_id")))
                .doctorId(readUuid(row.get("doctor_id")))
                .startTime(readInstant(row.get("start_time")))
                .endTime(readInstant(row.get("end_time")))
                .reserved(readBoolean(row.get("is_reserved")))
                .reservationToken(row.get("reservation_token", String.class))
                .reservedAt(readInstant(row.get("reserved_at")))
                .version(readLong(row.get("version")))
                .createdAt(readInstant(row.get("created_at")))
                .updatedAt(readInstant(row.get("updated_at")))
                .build();
    }

    private Instant readInstant(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Instant instant) {
            return instant;
        }
        if (value instanceof OffsetDateTime offsetDateTime) {
            return offsetDateTime.toInstant();
        }
        if (value instanceof LocalDateTime localDateTime) {
            return localDateTime.toInstant(ZoneOffset.UTC);
        }
        if (value instanceof java.sql.Timestamp timestamp) {
            return timestamp.toInstant();
        }
        throw new IllegalArgumentException("Unsupported temporal value: " + value.getClass());
    }

    private boolean isTruthy(Object value) {
        if (value instanceof Boolean bool) {
            return Boolean.TRUE.equals(bool);
        }
        if (value instanceof Number number) {
            return number.intValue() != 0;
        }
        return false;
    }

    private boolean readBoolean(Object value) {
        return isTruthy(value);
    }

    private Long readLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Number number) {
            return number.longValue();
        }
        throw new IllegalArgumentException("Unsupported numeric representation: " + value.getClass());
    }

    private UUID readUuid(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof UUID uuid) {
            return uuid;
        }
        if (value instanceof String string) {
            return UUID.fromString(string);
        }
        if (value instanceof byte[] bytes && bytes.length == 16) {
            ByteBuffer buffer = ByteBuffer.wrap(bytes);
            long high = buffer.getLong();
            long low = buffer.getLong();
            return new UUID(high, low);
        }
        throw new IllegalArgumentException("Unsupported UUID representation: " + value.getClass());
    }
}
