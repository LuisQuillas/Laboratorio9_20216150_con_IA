package com.example.laboratio9;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de la logica del tablero (no necesitan base de datos,
 * asi "mvn package" funciona aunque MySQL no este levantado).
 */
class Laboratio9ApplicationTests {

    @Test
    void desordenar3x3ConLaSecuenciaFija() {
        String[] t = Tablero.desordenar(3);
        assertEquals("15274386X", Tablero.aTexto(t));
        assertFalse(Tablero.estaResuelto(t));
    }

    @Test
    void desordenarFuncionaParaVariasDimensiones() {
        for (int n = Tablero.MIN_N; n <= Tablero.MAX_N; n++) {
            String[] t = Tablero.desordenar(n);
            assertEquals(n * n - 1, Tablero.indiceVacio(t));
            assertFalse(Tablero.estaResuelto(t));
            assertArrayEquals(t, Tablero.desdeTexto(Tablero.aTexto(t)));
        }
    }

    @Test
    void soloSeMuevenCasillasContiguas() {
        String[] t = Tablero.ordenado(3);       // vacio en la posicion 8
        assertFalse(Tablero.mover(t, 0));        // no contigua
        assertFalse(Tablero.mover(t, 4));        // diagonal
        assertTrue(Tablero.mover(t, 5));         // arriba del vacio
        assertEquals("X", t[5]);
    }

    @Test
    void resolverDeshaciendoLaSecuencia() {
        // Posiciones que se clicean para deshacer el desorden del 3x3
        String[] t = Tablero.desordenar(3);
        int[] clics = {7, 6, 3, 4, 1, 2, 5, 8};
        for (int p : clics) assertTrue(Tablero.mover(t, p));
        assertTrue(Tablero.estaResuelto(t));
    }

    @Test
    void validarFormatoDimension() {
        assertEquals(3, Tablero.leerDimension("3x3"));
        assertEquals(5, Tablero.leerDimension(" 5x5 "));
        assertEquals(-1, Tablero.leerDimension("3x4"));
        assertEquals(-1, Tablero.leerDimension("2x2"));
        assertEquals(-1, Tablero.leerDimension("3X3"));
        assertEquals(-1, Tablero.leerDimension("abc"));
        assertEquals(-1, Tablero.leerDimension(""));
    }
}
