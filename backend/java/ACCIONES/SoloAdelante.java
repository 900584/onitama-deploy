package ACCIONES;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import VO.CartaMov;
import VO.Partida;
import VO.Posicion;

public class SoloAdelante extends Accion {
    private Map<CartaMov, List<Posicion>> movimientosOriginales = new HashMap<>();

    public SoloAdelante() {
        super("SOLO_PARA_ADELANTE");
    }

    @Override
    public boolean ejecutar(Partida partida, int x, int y, int equipo, int xOp, int yOp, String nomCarta) {
        System.out.println("Ejecutando acción: " + getNombre());
        List<CartaMov> cartas = partida.getCartasMovimiento();
        movimientosOriginales.clear();

        for (CartaMov carta : cartas) {
            List<Posicion> posOriginales = new ArrayList<>(carta.getListaMovimientos());
            movimientosOriginales.put(carta, posOriginales);

            List<Posicion> movimientosFiltrados = new ArrayList<>();
            for (Posicion pos : posOriginales) {
                if (pos.getY() >= 0) { // Solo permite movimientos hacia adelante (Y positivo o 0)
                    movimientosFiltrados.add(pos);
                }
            }
            carta.setListaMovimientos(movimientosFiltrados);
        }
        return true;
    }

    @Override
    public void deshacer(Partida partida) {
        System.out.println("Deshaciendo acción: " + getNombre());
        for (Map.Entry<CartaMov, List<Posicion>> entry : movimientosOriginales.entrySet()) {
            entry.getKey().setListaMovimientos(entry.getValue());
        }
        movimientosOriginales.clear();
    }
}