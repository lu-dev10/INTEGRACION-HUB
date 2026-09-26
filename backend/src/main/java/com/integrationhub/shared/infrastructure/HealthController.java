package com.integrationhub.shared.infrastructure;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.sql.DataSource;
import java.sql.Connection;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/health")
@Tag(name = "Salud del Sistema", description = "Endpoints de diagnóstico de infraestructura")
public class HealthController {

    private final DataSource dataSource;

    public HealthController(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping
    @Operation(summary = "Verificar estado del servicio y conectividad con la BD")
    public ResponseEntity<Map<String, Object>> getHealth() {
        boolean dbConnected = false;
        String dbDetail = "OK";

        try (Connection conn = dataSource.getConnection()) {
            dbConnected = !conn.isClosed();
        } catch (Exception e) {
            dbDetail = e.getMessage() != null ? e.getMessage() : "Error al conectar con la base de datos";
        }

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("status", dbConnected ? "UP" : "DEGRADED");
        response.put("timestamp", Instant.now().toString());
        response.put("service", "integration-hub-backend");
        response.put("version", "1.0.0-MVP");

        Map<String, Object> dbInfo = new LinkedHashMap<>();
        dbInfo.put("connected", dbConnected);
        dbInfo.put("detail", dbDetail);
        response.put("database", dbInfo);

        return dbConnected ? ResponseEntity.ok(response) : ResponseEntity.status(503).body(response);
    }
}
