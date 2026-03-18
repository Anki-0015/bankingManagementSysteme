package com.example.banking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.Test;

import com.example.banking.security.JwtUtil;

public class JwtUtilTest {

    @Test
    void tokenLifecycle() {
        JwtUtil util = new JwtUtil("01234567890123456789012345678901", 10000);
        String token = util.generateToken("alice");
        assertNotNull(token);
        assertTrue(util.isTokenValid(token));
        assertEquals("alice", util.extractUsername(token));
    }
}
