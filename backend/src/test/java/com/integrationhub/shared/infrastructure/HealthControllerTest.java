package com.integrationhub.shared.infrastructure;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class HealthControllerTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @InjectMocks
    private HealthController healthController;

    @Test
    @DisplayName("Debe retornar UP (200 OK) cuando la base de datos responde exitosamente")
    void shouldReturnUpWhenDatabaseIsConnected() throws SQLException {
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isClosed()).thenReturn(false);

        ResponseEntity<Map<String, Object>> response = healthController.getHealth();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("UP", response.getBody().get("status"));
        assertEquals("integration-hub-backend", response.getBody().get("service"));

        @SuppressWarnings("unchecked")
        Map<String, Object> dbInfo = (Map<String, Object>) response.getBody().get("database");
        assertTrue((Boolean) dbInfo.get("connected"));
    }

    @Test
    @DisplayName("Debe retornar DEGRADED (503 Service Unavailable) cuando la base de datos falla")
    void shouldReturnDegradedWhenDatabaseFails() throws SQLException {
        when(dataSource.getConnection()).thenThrow(new SQLException("Connection refused"));

        ResponseEntity<Map<String, Object>> response = healthController.getHealth();

        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("DEGRADED", response.getBody().get("status"));

        @SuppressWarnings("unchecked")
        Map<String, Object> dbInfo = (Map<String, Object>) response.getBody().get("database");
        assertFalse((Boolean) dbInfo.get("connected"));
    }
}
