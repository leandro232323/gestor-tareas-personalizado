package com.example.demo.controllerTask;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import javax.sql.DataSource;

import java.sql.Statement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.time.LocalDate;
import com.example.demo.verificar.AlgoritmosVerificar;


@RestController
public class TaskController {
    
    @Autowired
    private DataSource dataSource;
    private AlgoritmosVerificar algoritVerify;

    @PostMapping("/agregarTarea")
    public Map<String, Object> taskAdd(@RequestBody Map<String, Object> requerimiento) {
        Map<String, Object> respuesta = new HashMap<>();   
        Connection conexion = null;
        PreparedStatement sentencia = null;
        ResultSet generatedKeys = null;

        try {
            // Validaciones
            String nombre = String.valueOf(requerimiento.get("nombre_tarea"));
            if(!algoritVerify.verificarNombre(nombre)) {
                respuesta.put("estado", false);
                respuesta.put("titulo", "ERROR con el campo (Nombre de la tarea)");
                respuesta.put("mensaje", "El nombre de la TAREA no puede tener numeros");
                return respuesta;
            }

            String materia = String.valueOf(requerimiento.get("nombre_materia"));
            String accion = String.valueOf(requerimiento.get("accion_tarea"));
            String fechaString = String.valueOf(requerimiento.get("fecha"));
            LocalDate fecha_entrega = LocalDate.parse(fechaString);
            Long cedulaSesion = Long.valueOf(String.valueOf(requerimiento.get("cedula_seccion")));

            String tablaActual = String.valueOf(requerimiento.get("tabla_actual"));
            String dia = filtrarDia(tablaActual);

            if(dia == null){
                respuesta.put("estado", false);
                respuesta.put("titulo", "Dia referenciado NULL");
                respuesta.put("mensaje", "El dia especificado no pudo ser referenciado");
                return respuesta;
            }

            conexion = dataSource.getConnection();    
            
            String sql = "INSERT INTO `dias`(`nombre`) VALUES (?)";
            sentencia = conexion.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            sentencia.setString(1, dia);
            int filas = sentencia.executeUpdate();

            generatedKeys = sentencia.getGeneratedKeys();
            int id_tabla_dias_Generado = 0;
            if (generatedKeys.next()) { // verficamos que exista el id generado a partir de la inserccion anterior
                id_tabla_dias_Generado = generatedKeys.getInt(1);  
            } else { // si no existe el id generado a partir de la inserccion entonces lanzamos error
                respuesta.put("estado", false);
                respuesta.put("titulo", "Error al agregar la tarea");
                respuesta.put("mensaje", "Error al obtener el ID generado a partir de la inserccion en la tabla dias");
                return respuesta;
            }
            generatedKeys.close();
            sentencia.close(); // cerramos la sentencia para poder hacer la siguiente inserccion

            if(filas == 0){ // quiere decir que no se pudo hacer la inserccion en la tabla "dias"
                respuesta.put("estado", false);
                respuesta.put("titulo", "Error al agregar la tarea");
                respuesta.put("mensaje", "Error a la hora de registrar en la tabla dias");
                return respuesta;
            }

  
            //despues de insertar en la tabla "dias" ahora insertamos en la tabla tareas
            String sql_2 = "INSERT INTO `tareas`(`nombre`, `materia`, `accion`, `entrega`, `id_dias`) VALUES (?,?,?,?,?)";
            sentencia = conexion.prepareStatement(sql_2, Statement.RETURN_GENERATED_KEYS);
            sentencia.setString(1, nombre);
            sentencia.setString(2, materia);
            sentencia.setString(3, accion);
            sentencia.setDate(4, java.sql.Date.valueOf(fecha_entrega));  
            sentencia.setInt(5, id_tabla_dias_Generado);
            filas = sentencia.executeUpdate();

            generatedKeys = sentencia.getGeneratedKeys();
            int id_tabla_tareas_Generado = 0;
            if (generatedKeys.next()) { // verficamos que exista el id generado a partir de la inserccion anterior
                id_tabla_tareas_Generado = generatedKeys.getInt(1);  
            } else { // si no existe el id generado a partir de la inserccion entonces lanzamos error
                respuesta.put("estado", false);
                respuesta.put("titulo", "Error al agregar la tarea");
                respuesta.put("mensaje", "Error al obtener el ID generado a partir de la inserccion en la tabla dias");
                return respuesta;
            }

            if(filas == 0){ // quiere decir que no se hizo la inserccion en la tabla "tareas"
                respuesta.put("titulo", "Error fatal");
                respuesta.put("estado", false);
                respuesta.put("mensaje", "Error a la hora de agregar una tarea");
                return respuesta;
            }


            generatedKeys.close();
            sentencia.close(); // cerramos la sentencia para poder hacer la siguiente inserccion

            //ahora por ultimo hacemos la inserccion en la ultima tabla
            String sql_3 = "INSERT INTO `usuarios_tareas`(`usuario_cedula`, `tarea_id`) VALUES (?,?)";
            sentencia = conexion.prepareStatement(sql_3, Statement.RETURN_GENERATED_KEYS);
            sentencia.setLong(1, cedulaSesion);
            sentencia.setInt(2, id_tabla_tareas_Generado);
            filas = sentencia.executeUpdate();


            if(filas == 0){ // quiere decir que no se hizo la inserccion en la tabla "tareas"
                respuesta.put("titulo", "Error fatal");
                respuesta.put("estado", false);
                respuesta.put("mensaje", "Error a la hora de agregar una tarea");
                return respuesta;
            }


            respuesta.put("titulo", "Agregado con exito");
            respuesta.put("estado", true);
            respuesta.put("mensaje", "Tarea agregada con exito");

        } catch (SQLException e) {
            respuesta.put("titulo", "Error fatal");
            respuesta.put("estado", false);
            respuesta.put("mensaje", e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            respuesta.put("titulo", "Error inesperado");
            respuesta.put("estado", false);
            respuesta.put("mensaje", e.getMessage());
            e.printStackTrace();
        } finally {
            // Cerrar recursos
            try {
                if (generatedKeys != null) generatedKeys.close();
                if (sentencia != null) sentencia.close();
                if (conexion != null) conexion.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        return respuesta;
    }


    public static String filtrarDia(String tablaActual){
        Map<String, String> dias = new HashMap<>();
        String diaFiltradp = "";
        dias.put("tablaLunes", "Lunes");
        dias.put("tablaMartes", "Martes");
        dias.put("tablaMiercoles", "Miercoles");
        dias.put("tablaJueves", "Jueves");
        dias.put("tablaViernes", "Viernes");
        dias.put("tablaSabado", "Sabado");
        dias.put("tablaDomingo", "Domingo");

        for(String clave: dias.keySet()){ //recorremos las claves primero y despues de eso accedemos 
            // a sus valores
            if(tablaActual.equalsIgnoreCase(clave)){
                diaFiltradp = dias.get(clave);
                return diaFiltradp;
            }
        }
        return null;
    }


    

}
