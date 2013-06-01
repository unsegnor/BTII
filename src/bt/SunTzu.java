/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package bt;

import bt.Reglas.Fase;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Clase que centraliza toda la elaboración de la estrategia.
 *
 * @author Víctor
 */
public class SunTzu {
    private static int p_encaramiento = 1;
    private static int p_nivel = 1;
    private static int p_cobertura = 2;
    private static int p_cobertura_enemigo = 2;

    public static Accion siguienteAccion(int jugador, Fase fase) {


        EstadoDeJuego estado = leer_estado_actual(jugador, fase);


        //Responder en función de la fase
        Accion resultado = null;


        switch (fase) {
            case Movimiento:
                resultado = responderAMovimiento(estado);
        }



        //Devolver el mejor siguiente movimiento


        return resultado;
    }

    /**
     * La estrategia para la fase de movimiento será: Buscar la espalda del
     * enemigo Encararlo Buscar cobertura Estar más alto que el enemigo para
     * ignorar su cobertura
     *
     *
     * @param estado
     * @return
     */
    private static Accion responderAMovimiento(EstadoDeJuego estado) {
        Accion resultado = null;


        //Si somos los últimos o primeros en mover debe influir en nuestra decisión



        //Comprobar posiciones de los mechs enemigos operativos
        //y la nuestra

        ArrayList<DataMech> enemigos_operativos = new ArrayList<DataMech>();
        DataMech mech_actual = null;

        DataMech[] mechs = estado.datos_mechs.getMechs();

        for (DataMech mech : mechs) {
            if (mech.getnJugador() == estado.jugador) {
                mech_actual = mech;
            } else {
                if (mech.isOperativo()) {
                    enemigos_operativos.add(mech);
                }
            }
        }

        //Seleccionar un subconjunto de casillas alrededor del mech (según su capacidad de movimiento) o evaluarlas todas
        Phexagono posicion_mech = new Phexagono(mech_actual.getColumna(), mech_actual.getFila());
        int pAndar = mech_actual.getInformacion_adicional().getPuntos_andar();
        int pCorrer = mech_actual.getInformacion_adicional().getPuntos_correr();
        int pSaltar = mech_actual.getInformacion_adicional().getPuntos_saltar();
        //Los puntos de movimiento máximos de los 3
        int max_pm = Math.max(pAndar, Math.max(pCorrer, pSaltar));

        ArrayList<Phexagono> posiciones_cercanas = estado.mapa.cercanas(posicion_mech, max_pm);

        //Ordenar las posiciones en función de la ventaja que nos confieren
        ArrayList<Evaluacion<Phexagono>> evaluaciones = new ArrayList<Evaluacion<Phexagono>>();

        //Evaluar todas las casillas
        for (Phexagono p : posiciones_cercanas) {
            double bondad = evaluar(p, estado, enemigos_operativos);
            evaluaciones.add(new Evaluacion(p, bondad));
        }

        //Ordenar las casillas
        Collections.sort(evaluaciones, new OrdenadorDeEvaluaciones());
        
        //Buscar una ruta desde la posición actual a las mejores hasta hallar la primera
        //TODO de momento saltamos, luego hay que implementar el correr y andar
        
        //Para cada posición ordenadas de mejor a peor buscar una ruta andando, corriendo o saltando
        //en el momento en que se encuentre ese será el movimiento a realizar
        for(Evaluacion e : evaluaciones){
            Phexagono pe = (Phexagono) e.cosa;
            System.out.println(pe.getColumna() + "," + pe.getFila() + " -> " + e.valor);
            
            
            //Comprobar si se puede llegar andando
            //Sino comprobar si se puede llegar corriendo
            //Sino comprobar si se puede llegar saltando
        }
        

        
        
        
        //Movernos a aquella a la que se pueda llegar


        return resultado;
    }

    private static EstadoDeJuego leer_estado_actual(int jugador, Fase fase) {
        EstadoDeJuego estado = new EstadoDeJuego();
        estado.jugador = jugador;
        estado.fase = fase;


        //Leer el mapa
        DataMapa dmapa = Cargador.cargarMapa(jugador);
        Mapa mapa = new Mapa(dmapa);
        //Escribir el mapa
        estado.mapa = mapa;

        //Leer los mechs
        DataMechs dm = Cargador.cargarMech(jugador);
        //Escribirlos
        estado.datos_mechs = dm;


        return estado;
    }

    /**
     * Evalua la bondad de una posición en el mapa
     *
     * @param p
     * @param estado
     * @param enemigos_operativos
     * @return
     */
    private static double evaluar(Phexagono p, EstadoDeJuego estado, ArrayList<DataMech> enemigos_operativos) {
        //Aquí es donde puntuamos lo bien o mal que podría estar la posición respecto a los enemigos
        double resultado = 0;
        //Para cada enemigo
        for (DataMech enemigo : enemigos_operativos) {
            resultado += evaluar(p, estado, enemigo);
        }

        return resultado;
    }

    private static double evaluar(Phexagono p, EstadoDeJuego estado, DataMech enemigo) {
        //Aquí es donde puntuamos lo bien o mal que podría estar la posición respecto un ememigo concreto
        double resultado = 0;

        Phexagono p_enemigo = new Phexagono(enemigo.getColumna(), enemigo.getFila());
        Hexagono h_enemigo = estado.mapa.casilla(p_enemigo);
        Hexagono h = estado.mapa.casilla(p);

        //Es mejor que esté detrás de él que delante
        //Depende hacia dónde esté mirando el enemigo
        int encaramiento_enemigo = enemigo.getEncaramientoMech();

        //Si está mirando hacia arriba
        if (encaramiento_enemigo == 1) {
            //Y la posición está por delante de él entonces la penalizamos
            if (p.getFila() < p_enemigo.getFila()) {
                resultado += -p_encaramiento;
            } else {
                //Si está por detrás la incentivamos
                resultado += p_encaramiento;
            }
        } else if (encaramiento_enemigo == 2) {
            //Si está mirando hacia el noreste
            if (p.getFila() > p_enemigo.getFila()) {
                resultado += p_encaramiento;
            } else {
                //Si está por detrás la incentivamos
                resultado += -p_encaramiento;
            }
            if (p.getColumna() < p_enemigo.getColumna()) {
                resultado += p_encaramiento;
            } else {
                //Si está por detrás la incentivamos
                resultado += -p_encaramiento;
            }
        }else if (encaramiento_enemigo == 3) {
            //Si está mirando hacia el sureste
            if (p.getFila() < p_enemigo.getFila()) {
                resultado += p_encaramiento;
            } else {
                //Si está por detrás la incentivamos
                resultado += -p_encaramiento;
            }
            if (p.getColumna() < p_enemigo.getColumna()) {
                resultado += p_encaramiento;
            } else {
                //Si está por detrás la incentivamos
                resultado += -p_encaramiento;
            }
        }else if (encaramiento_enemigo == 4) {
            //Si está mirando hacia el sur
            if (p.getFila() < p_enemigo.getFila()) {
                resultado += p_encaramiento;
            } else {
                //Si está por detrás la incentivamos
                resultado += -p_encaramiento;
            }
        }else if (encaramiento_enemigo == 5) {
            //Si está mirando hacia el suroeste
            if (p.getFila() < p_enemigo.getFila()) {
                resultado += p_encaramiento;
            } else {
                //Si está por detrás la incentivamos
                resultado += -p_encaramiento;
            }
            if (p.getColumna() > p_enemigo.getColumna()) {
                resultado += p_encaramiento;
            } else {
                //Si está por detrás la incentivamos
                resultado += -p_encaramiento;
            }
        }else if (encaramiento_enemigo == 6) {
            //Si está mirando hacia el noroeste
            if (p.getFila() > p_enemigo.getFila()) {
                resultado += p_encaramiento;
            } else {
                //Si está por detrás la incentivamos
                resultado += -p_encaramiento;
            }
            if (p.getColumna() > p_enemigo.getColumna()) {
                resultado += p_encaramiento;
            } else {
                //Si está por detrás la incentivamos
                resultado += -p_encaramiento;
            }
        }

        //Es mejor que esté por encima, cuanto más alto mejor
        int diferencia_nivel = h.getNivel() - h_enemigo.getNivel();
        resultado += diferencia_nivel * p_nivel;

        //Procuramos que haya cobertura
        //Calculamos la LDV y cobertura
        ResultadoLDV rLDV = LDV.calcularLDV(estado.mapa, p, 1, p_enemigo, 1);
        
        if(rLDV.LDV){
            if(rLDV.CPdirecta){
                resultado += -p_cobertura_enemigo;
            }
            if(rLDV.CPinversa){
                resultado += +p_cobertura;
            }
        }
        
        return resultado;
    }
}
