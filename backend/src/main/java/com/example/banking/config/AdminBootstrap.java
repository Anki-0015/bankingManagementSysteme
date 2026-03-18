package com.example.banking.config;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.banking.model.Role;
import com.example.banking.model.User;
import com.example.banking.repository.UserRepository;

@Component
public class AdminBootstrap {
    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    @Value("${app.admin.enabled:false}")
    private boolean enabled;
    @Value("${app.admin.username:admin}")
    private String username;
    @Value("${app.admin.email:admin@local}")
    private String email;
    @Value("${app.admin.password:ChangeMe123!}")
    private String password;
    @Value("${app.admin.sync-password-on-start:false}")
    private boolean syncPassword;

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminBootstrap(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @EventListener(ApplicationReadyEvent.class)
    public void createDefaultAdmin() {
        log.info("AdminBootstrap start: enabled={} username={} email={} (password length={})", enabled, username, email, password == null ? 0 : password.length());
        if(!enabled) {
            log.info("Admin bootstrap disabled. Skipping creation.");
            return;
        }
        // If an admin already exists, optionally sync password if enabled
        boolean hasAdmin = userRepository.existsByRole(Role.ADMIN);
        if(hasAdmin) {
            if(syncPassword) {
                Optional<User> existing = userRepository.findByUsername(username);
                if(existing.isPresent()) {
                    User u = existing.get();
                    if(u.getRole() == Role.ADMIN) {
                        if(!passwordEncoder.matches(password, u.getPasswordHash())) {
                            u.setPasswordHash(passwordEncoder.encode(password));
                            userRepository.save(u);
                            log.warn("Admin password for '{}' was updated from application properties.", username);
                        } else {
                            log.info("Admin password already matches configured password (username='{}').", username);
                        }
                    } else {
                        log.warn("A user named '{}' exists but is not ADMIN. Password not synced.", username);
                    }
                } else {
                    log.info("An ADMIN exists (different username). No new admin created. To manage, adjust existing admin user or remove it if you want this configured one.");
                }
            } else {
                log.info("Admin already present. (Password sync disabled). No default admin will be created.");
            }
            return;
        }
        if(userRepository.existsByUsername(username)) {
            log.warn("User with configured admin username '{}' already exists but is not ADMIN. Not modifying it.", username);
            return; // don't overwrite existing user with same username
        }
        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setRole(Role.ADMIN);
        userRepository.save(user);
        log.warn("Default ADMIN user created with username='{}' PLEASE CHANGE THE PASSWORD SOON.", username);
    }

}
