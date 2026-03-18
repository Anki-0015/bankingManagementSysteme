package com.example.banking.config;

import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

/**
 * Repairs legacy or invalid entries in users.role before application logic relies on enum mapping.
 * Converts empty strings / NULL to 'USER' and logs any unknown values.
 */
@Component
public class RoleColumnRepair {
    private static final Logger log = LoggerFactory.getLogger(RoleColumnRepair.class);
    private final JdbcTemplate jdbc;
    private static final Set<String> VALID = Set.of("USER","ADMIN");

    public RoleColumnRepair(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void repair() {
        try {
            // Ensure column type is VARCHAR (skip failures silently if table not yet created)
            jdbc.execute("ALTER TABLE users MODIFY COLUMN role varchar(20) NOT NULL");
        } catch (Exception ignored) {
            // Table may not exist on first run; Hibernate will create it. Ignore.
        }
        try {
            // Normalize null or empty
            int fixedNull = jdbc.update("UPDATE users SET role='USER' WHERE role IS NULL OR role='' ");
            if (fixedNull > 0) {
                log.warn("RoleColumnRepair: Set role='USER' for {} records with NULL/empty role", fixedNull);
            }
            // Find invalid values
            List<String> invalid = jdbc.query("SELECT DISTINCT role FROM users", (rs, i) -> rs.getString(1));
            invalid.stream().filter(v -> v != null && !VALID.contains(v)).forEach(v -> {
                log.error("RoleColumnRepair: Found invalid role value '{}' in database. Consider correcting it manually.", v);
            });
        } catch (Exception e) {
            log.debug("RoleColumnRepair skipped (table not ready yet): {}", e.getMessage());
        }
    }
}
