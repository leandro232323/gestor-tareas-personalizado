package com.example.demo;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

@RestController
public class Conexion {

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @GetMapping("/test-conexion")
    public Map<String, Object> testConexion() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // Intentar conexión
            Connection connection = dataSource.getConnection();
            // Verificar si está conectado
            if (connection != null && !connection.isClosed()) {
                
                // Intentar consulta simple
                String result = jdbcTemplate.queryForObject("SELECT 'Conexión exitosa a MySQL'", String.class);
                String databaseName = connection.getCatalog();
                
                connection.close();
                
                // Respuesta JSON estructurada
                response.put("success", true);
                response.put("message", result);
                response.put("database", databaseName);
                
                return response;
            }
            
        } catch (SQLException e) {
            response.put("success", false);
            response.put("error", "Error de conexión: " + e.getMessage());
            return response;
        }
        
        response.put("success", false);
        response.put("error", "No se pudo establecer conexión");
        return response;
    }

}