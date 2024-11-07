package com.aluracursos.screenmatch.principal;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EjemploStreams {
    public void muestraEjemplo(){
        List<String> nombres= Arrays.asList("Anibal","Gerdi","Deya","Michael","Joaquin","Sonia");
        nombres.stream()
                .sorted()
                .limit(4)
                .map(n->n.toUpperCase())
                .forEach(System.out::println);
    }
}
