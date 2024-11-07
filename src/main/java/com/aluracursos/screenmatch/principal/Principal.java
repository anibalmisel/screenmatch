package com.aluracursos.screenmatch.principal;

import com.aluracursos.screenmatch.model.DatosEpisode;
import com.aluracursos.screenmatch.model.DatosSerie;
import com.aluracursos.screenmatch.model.DatosTemporada;
import com.aluracursos.screenmatch.model.Episodio;
import com.aluracursos.screenmatch.service.ConsumoAPI;
import com.aluracursos.screenmatch.service.ConvierteDatos;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class Principal {
    private Scanner teclado = new Scanner(System.in);
    private ConsumoAPI consumoApi= new ConsumoAPI();
    private final String URL_BASE="http://www.omdbapi.com/?t=";
    private final String API_KEY="&apikey=ab8fbc67";
    private ConvierteDatos conversor=new ConvierteDatos();

    public void muestraElMenu(){
        boolean exit=true;
        while (exit==true){
            System.out.println("Por favor escribe el nombre de la serie que desea buscar: ");
            var nombreSerie=teclado.nextLine().toLowerCase();

            var json=consumoApi.obtenerDatos(URL_BASE+nombreSerie.replace(" ", "+" )+API_KEY);
            var datos= conversor.obtenerDatos(json, DatosSerie.class);

            System.out.println(datos);
            //if para excluir nombres de series que no tengan datos o que hayan sido escritos erroneamente
            if ((datos.titulo() == null || datos.titulo().equalsIgnoreCase("null")) &&
                    (datos.totalDeTemporadas() == null || datos.totalDeTemporadas() == 0) &&
                    (datos.evaluacion() == null || datos.evaluacion().equalsIgnoreCase("null"))) {
                System.out.println("No hay datos para esa serie ¿Escribió bien el nombre de la serie?");
                System.out.println("Vuelva a intentarlo:");
            } else{
                System.out.println(" ");
                //imprime lista de temporadas
                List<DatosTemporada> listaTemporadas=new ArrayList<>();
                for (int i = 1; i <= datos.totalDeTemporadas() ; i++) {
                    json= consumoApi.obtenerDatos(URL_BASE+nombreSerie.replace(" ", "+" )+"&Season="+i+API_KEY);
                    var datosTemporadas= conversor.obtenerDatos(json,DatosTemporada.class);
                    listaTemporadas.add(datosTemporadas);
                }
                listaTemporadas.forEach(System.out::println);
                System.out.println(" ");
                //Mostrar solo el titulo de los episodios por temporadas
                //listaTemporadas.forEach(t->t.episodios().forEach(e-> System.out.println(e.titulo())));
                System.out.println(" ");
                //List<Integer> lista = IntStream.rangeClosed(1, 9).boxed().collect(Collectors.toList());Crea una lista de números del 1 al 9.

                for (int i = 0; i < datos.totalDeTemporadas() ; i++) {
                    List<DatosEpisode>episodiosTemporada=listaTemporadas.get(i).episodios();
                    System.out.println("Temporada "+(i+1));
                    for (int j = 0; j < episodiosTemporada.size(); j++) {
                        System.out.println(" "+(j+1)+") "+episodiosTemporada.get(j).titulo());
                    }
                    System.out.println(" ");
                }


                //Convertir todas las informaciones en una lista del tipo DatosEpisodio
                List<DatosEpisode> datosEpisode=listaTemporadas.stream()
                        .flatMap(t->t.episodios().stream())
                        .collect(Collectors.toList());

                //Top 5 episodios

                System.out.println("TOP 5 EPISODIOS");
                datosEpisode.stream()
                        .filter(e->!e.evaluacion().equalsIgnoreCase("N/A"))
                        .sorted(Comparator.comparing(DatosEpisode::evaluacion).reversed())
                        .limit(5)
                        .forEach(System.out::println);

                System.out.println(" ");

                //Convirtiendo los datos a una lista del tipo episodio
                List<Episodio>episodios=listaTemporadas.stream()
                                .flatMap(t->t.episodios().stream()
                                        .map(d->new Episodio(t.numero(), d)))
                                        .collect(Collectors.toList());
                episodios.forEach(System.out::println);
                //Busqueda de episodios a partir de un año especifico

                System.out.println(" ");
                System.out.println("Indique el año que desea que inicie la búsqueda");
                int ano= teclado.nextInt();
                teclado.nextLine();
                LocalDate fechaBusqueda=LocalDate.of(ano,1,1);
                DateTimeFormatter dtf=DateTimeFormatter.ofPattern("dd/MM/yyyy");
                episodios.stream()
                        .filter(e->e.getFechaDeLanzamiento()!= null && e.getFechaDeLanzamiento().isAfter(fechaBusqueda))
                        .forEach(e-> System.out.println(
                                "Temporada "+e.getTemporada()+
                                        " Episodio "+e.getTitulo()+
                                        " Fecha de lanzamiento "+e.getFechaDeLanzamiento().format(dtf)
                        ));
                System.out.println(" ");

                //Busca episodio por pedazo de titulo

                System.out.println("Por favor escriba titulo del espisodio que desea ver:");
                String titulo= teclado.nextLine().toLowerCase();

               //Ni findFirst, ni Any, distinct descarta duplicados y crea una lista
                List<Episodio> episodioBuscado=episodios.stream()
                        .filter(e->e.getTitulo().toLowerCase().contains(titulo))
                        .distinct()
                        .limit(3)
                        .collect(Collectors.toList());

                if (episodioBuscado.isEmpty()==false) {
                    System.out.println("Episodios encontrados");
                    episodioBuscado.forEach(System.out::println);
                }else {
                    System.out.println("Episodio no encontrado");
                }
                System.out.println(" ");

                Map<Integer, Double> evaluacionesPorTemporadas= episodios.stream()
                        .filter(e->e.getEvaluacion()>0.0)
                        .collect(Collectors.groupingBy(Episodio::getTemporada,
                             Collectors.averagingDouble(Episodio::getEvaluacion)));
                System.out.println(evaluacionesPorTemporadas);
                System.out.println(" ");

                DoubleSummaryStatistics est = episodios.stream()//crea algunas estadisticas
                        .filter(e->e.getEvaluacion()>0.0)
                        .collect(Collectors.summarizingDouble(Episodio::getEvaluacion));
                System.out.println("Media de todas las evaluaciones: "+est.getAverage());
                System.out.println("Episodio mejor evaluado: "+est.getMax());
                System.out.println("Episodio peor evaluado: "+est.getMin());
                System.out.println(" ");
                System.out.println(est.toString());
            }

                System.out.println("¿Desea buscar otra serie? s/n:");
                String siNo= teclado.nextLine();
                if (siNo.equalsIgnoreCase("s")) {
                    exit=true;
                } else {
                    exit=false;
                    System.out.println("¡Hasta pronto!");
                }
        }
    }
}