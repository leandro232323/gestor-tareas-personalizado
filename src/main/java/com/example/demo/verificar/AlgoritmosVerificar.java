package com.example.demo.verificar;

import java.util.Arrays;
import java.util.List;

public class AlgoritmosVerificar {

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
