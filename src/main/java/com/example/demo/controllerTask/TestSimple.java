package com.example.demo.controllerTask;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TestSimple {
    public static void main(String[] args) {
        if(verificarNombre("sdfgdfg23rs gsfgf") == false){
            System.out.println("INVALIDO");
            return;
        }

        System.out.println("Validado");
    }

    public static Boolean verificarNombre(String nombre){
        List<String> numeros = Arrays.asList("0","1","2","3","4","5","6","7","8","9");

        for(int x = 0; x < nombre.length(); x++){
            String letra_string = String.valueOf(nombre.charAt(x));
            Boolean existe = numeros.contains(letra_string);
            if(existe == true){
                return false;
            }
        }
        return true;
    }

}
