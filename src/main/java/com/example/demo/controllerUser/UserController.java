package com.example.demo.controllerUser;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.sql.DataSource;

import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


import java.util.HashMap;
import java.util.Map;


@RestController
public class UserController {

    @Autowired
    private DataSource dataSource;

    @PostMapping("/registro")
    public Map<String, Object> registrar(@RequestBody Map<String, Object> requerimiento) {
        Map<String, Object> respuesta = new HashMap<>();
        
        Connection connection = null;
        ResultSet resultado = null;
        PreparedStatement sentencia = null;

        try{

            String cedula = (String) requerimiento.get("cedula");
            String nombre = (String) requerimiento.get("nombre");
            String correo = (String) requerimiento.get("correo");
            String password = (String) requerimiento.get("password");

            BigInteger cedulaNum;

            try {
                cedulaNum = new BigInteger(cedula);
            } catch (NumberFormatException e) {
                respuesta.put("success", false);
                respuesta.put("titulo", "Cedula Invalida");
                respuesta.put("mensaje", e.getMessage());
                return respuesta;
            }

            //Validamos la longitud de la cedula
            BigInteger limiteSuperior = new BigInteger("10000000000");
            BigInteger limiteInferior = new BigInteger("1000000");

            if(cedulaNum.compareTo(limiteInferior) < 0 || cedulaNum.compareTo(limiteSuperior) > 0){
                respuesta.put("success", false);
                respuesta.put("titulo", "Cedula Invalida");
                respuesta.put("mensaje", "Una cedula valida tiene entre 6 a 10 digitos");
                return respuesta;
            }

            //validamos que el correo con el que se intenta registrar NO EXISTA
            Map<String, Object> valid_Email_Boolean = existe_correo(correo); 
            if((Boolean) valid_Email_Boolean.get("success") == true ){ // si se encuentra registrado, no se puede agregar.
                String validemail_titulo = String.valueOf(valid_Email_Boolean.get("titulo"));
                String validEmail_error = String.valueOf(valid_Email_Boolean.get("error"));

                respuesta.put("success", false);
                respuesta.put("titulo", validemail_titulo);
                respuesta.put("mensaje", validEmail_error);
                return respuesta;
            }

            connection = dataSource.getConnection();
            sentencia = null;
            String sql = "INSERT INTO `usuarios`(`cedula`, `nombre`, `correo`, `password`) VALUES (?,?,?,?)";

            sentencia = connection.prepareStatement(sql);

            sentencia.setLong(1, cedulaNum.longValue());  // Para BIGINT en MySQL
            sentencia.setString(2, nombre);
            sentencia.setString(3, correo);
            sentencia.setString(4, password);
            
            //Ejecutamos la sentencia
            int filasAfectadas = sentencia.executeUpdate();
        
            if (filasAfectadas > 0) {
                respuesta.put("success", true);
                respuesta.put("titulo", "Registro exitoso");
                respuesta.put("mensaje", "Usuario registrado correctamente");
            } else {
                respuesta.put("success", false);
                respuesta.put("titulo", "Error en registro");
                respuesta.put("mensaje", "No se pudo insertar el usuario");
            }
            
        }catch(SQLException e){
            respuesta.put("success", false);
            respuesta.put("titulo", "Error fatal");
            respuesta.put("mensaje", e.getMessage());
        } finally{
            try {
                if (resultado != null) resultado.close();
                if (sentencia != null) sentencia.close();
                if (connection != null) connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } 
        }
        return respuesta;
    }


    //Funcion para verificar si un correo ya existe en la BASE DE DATOS.
    public Map<String, Object> existe_correo(String correo){

        Connection conexion = null;
        ResultSet resultado = null;
        PreparedStatement sentencia = null;
        Map<String, Object> datos = new HashMap<>();

        try {

            conexion = dataSource.getConnection();
            String sql = "SELECT * FROM `usuarios` WHERE `correo` = ?";
            sentencia = conexion.prepareStatement(sql);

            sentencia.setString(1, correo);
            resultado = sentencia.executeQuery();

            if(resultado.next() == false){ //No existe el correo
                datos.put("success", false);
                return datos;
            }

            datos.put("success", true);
            datos.put("titulo", "Ya se encuentra registrado");
            datos.put("error", "Registrese con otro correo, por que este " + correo + " ya se encuentra en uso");

        } catch (SQLException e) {
            datos.put("success", false);
            datos.put("titulo", "Error fatal");
            datos.put("error", e.getMessage());
        } finally{
            try {
                if(conexion != null) conexion.close();
                if(resultado != null) resultado.close();
                if(sentencia != null) sentencia.close();
            } catch (SQLException e) {
                datos.put("success", false);
                datos.put("titulo", "Error a la hora de cerra conexion en la funcion existe_correo()");
                datos.put("error", e.getMessage());
            }
        }
        return datos;
    }




    @PostMapping("/iniciar")
    public Map<String, Object> iniciar(@RequestBody Map<String, Object> requerimiento) {
        Map<String, Object> respuesta = new HashMap<>();

        String correo = String.valueOf(requerimiento.get("correo_auth"));
        String password = String.valueOf(requerimiento.get("password_auth"));
        long cedulaSeccion = 0L;  // ✅ Cambiar a long
        
        Connection conexion = null;
        PreparedStatement sentencia = null;
        ResultSet resultado = null;

        if(correo == null || password == null){
            respuesta.put("success", false);
            respuesta.put("titulo", "Campos invalidos");
            respuesta.put("error", "Los campos de correo y password son NULL");
            return respuesta;
        }

        if(correo.equalsIgnoreCase("") || password.equalsIgnoreCase("")){
            respuesta.put("success", false);
            respuesta.put("titulo", "Campos vacios");
            respuesta.put("error", "Por favor llene todos los campos");
            return respuesta;
        }
        
        try {
            conexion = dataSource.getConnection();
            sentencia = null;
            String sql = "SELECT `cedula`, `nombre`, `correo`, `password` FROM `usuarios` WHERE `correo` = ?";
            
            sentencia = conexion.prepareStatement(sql);
            sentencia.setString(1, correo);

            resultado = sentencia.executeQuery();

            if(resultado.next() == false){ // No existe la condicion del sql
                respuesta.put("success", false);
                respuesta.put("titulo", "No se encuentra registrado");
                respuesta.put("error", "El correo con el que intenta acceder no se encuentra registrado");
                return respuesta;
            }

            String password_database = resultado.getString("password");
            cedulaSeccion = resultado.getLong("cedula");  // ✅ getLong para BIGINT

            if(!password_database.equalsIgnoreCase(password)){
                respuesta.put("success", false);
                respuesta.put("titulo", "Credenciales incorrectas");
                respuesta.put("error", "EL correo y la password no coinciden");
                return respuesta;
            }

            respuesta.put("success", true);
            respuesta.put("titulo", "Iniciando session");
            respuesta.put("mensaje", "datos validados");
            respuesta.put("cedulaSeccion", cedulaSeccion);
            
        } catch (SQLException e) {
            respuesta.put("success", false);
            respuesta.put("titulo", "Error fatal");
            respuesta.put("error", e.getMessage());
        } finally{
            try {
                if (resultado != null) resultado.close();
                if (sentencia != null) sentencia.close();
                if (conexion != null) conexion.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } 
        }
        
        return respuesta;
    }
    
    
/*
   para todas las operaciones CRUD es con sentencia.executeUpdate();, eso devuelve el numero de filas afectadas
   solo cambia para BUSCAR, que es con executeQuery y eso se tiene que guardar en una variable de tipo Resulset
   es como una tabla virtual que contiene tods los datos de la consulta SELECT
  
 */
    
    
}
