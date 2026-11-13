package com.example.laboratio9;

/**
 * Logica del tablero del Stf-Puzzle.
 *
 * Representacion: un arreglo de N*N textos en orden fila por fila.
 * Las figuras se numeran 1..(N*N-1) segun su posicion en la imagen ORIGINAL
 * y el espacio vacio es "X". El tablero resuelto es 1,2,3,...,N*N-1,X.
 *
 * En la BD se guarda como texto:
 *  - 3x3 (numeros de un digito): "13652847X"  (igual que el ejemplo del enunciado)
 *  - 4x4 o mas (numeros de dos digitos): "1,2,3,...,15,X" (separado por comas)
 */
public class Tablero {

    public static final String VACIO = "X";

    /**
     * UNICA secuencia fija para desordenar (8 pasos, minimo pedido por el enunciado).
     * Se aplica "de atras hacia adelante": se parte de la imagen ORDENADA y se mueve
     * el espacio vacio (que empieza en la esquina inferior derecha). Cada paso es un
     * movimiento valido, por lo que el juego siempre tiene solucion.
     * La secuencia vuelve a dejar el vacio en la esquina inferior derecha.
     */
    public static final String[] SECUENCIA_DESORDEN = {
            "ARRIBA", "ARRIBA", "IZQUIERDA", "ABAJO", "IZQUIERDA", "ABAJO", "DERECHA", "DERECHA"
    };

    public static final int MIN_N = 3;
    public static final int MAX_N = 10;

    /** Tablero resuelto: 1..N*N-1 y la ultima casilla vacia. */
    public static String[] ordenado(int n) {
        String[] t = new String[n * n];
        for (int i = 0; i < n * n - 1; i++) {
            t[i] = String.valueOf(i + 1);
        }
        t[n * n - 1] = VACIO;
        return t;
    }

    /** Aplica la secuencia fija de desorden sobre el tablero ordenado. */
    public static String[] desordenar(int n) {
        String[] t = ordenado(n);
        int vacio = n * n - 1;
        for (String paso : SECUENCIA_DESORDEN) {
            int fila = vacio / n;
            int col = vacio % n;
            switch (paso) {
                case "ARRIBA" -> fila--;
                case "ABAJO" -> fila++;
                case "IZQUIERDA" -> col--;
                case "DERECHA" -> col++;
            }
            int destino = fila * n + col;
            intercambiar(t, vacio, destino);
            vacio = destino;
        }
        return t;
    }

    public static int indiceVacio(String[] t) {
        for (int i = 0; i < t.length; i++) {
            if (VACIO.equals(t[i])) return i;
        }
        return -1;
    }

    /** true si las casillas a y b son vecinas (arriba, abajo, izquierda o derecha). */
    public static boolean sonContiguas(int a, int b, int n) {
        int fa = a / n, ca = a % n;
        int fb = b / n, cb = b % n;
        return Math.abs(fa - fb) + Math.abs(ca - cb) == 1;
    }

    /**
     * Mueve la figura de la casilla "posicion" al espacio vacio.
     * @return false si el movimiento no es valido (no es contigua al vacio)
     */
    public static boolean mover(String[] t, int posicion) {
        int n = dimension(t);
        if (posicion < 0 || posicion >= t.length) return false;
        int vacio = indiceVacio(t);
        if (!sonContiguas(posicion, vacio, n)) return false;
        intercambiar(t, posicion, vacio);
        return true;
    }

    public static boolean estaResuelto(String[] t) {
        String[] resuelto = ordenado(dimension(t));
        for (int i = 0; i < t.length; i++) {
            if (!resuelto[i].equals(t[i])) return false;
        }
        return true;
    }

    public static int dimension(String[] t) {
        return (int) Math.round(Math.sqrt(t.length));
    }

    /** Convierte el tablero al texto que se guarda en la columna stf_game_board_structure. */
    public static String aTexto(String[] t) {
        boolean unDigito = t.length - 1 <= 9;
        return String.join(unDigito ? "" : ",", t);
    }

    /** Convierte el texto de la BD al tablero. Devuelve null si el texto no es valido. */
    public static String[] desdeTexto(String texto) {
        if (texto == null || texto.isBlank()) return null;
        String[] t = texto.contains(",") ? texto.split(",") : texto.split("");
        int n = dimension(t);
        if (n * n != t.length || n < MIN_N || indiceVacio(t) < 0) return null;
        return t;
    }

    /**
     * Valida el formato de dimensiones "NxN" (ej. 3x3, 4x4) y devuelve N.
     * @return N, o -1 si el formato no es valido
     */
    public static int leerDimension(String texto) {
        if (texto == null) return -1;
        String s = texto.trim();
        if (!s.matches("\\d{1,2}x\\d{1,2}")) return -1;
        String[] partes = s.split("x");
        int filas = Integer.parseInt(partes[0]);
        int columnas = Integer.parseInt(partes[1]);
        if (filas != columnas || filas < MIN_N || filas > MAX_N) return -1;
        return filas;
    }

    private static void intercambiar(String[] t, int a, int b) {
        String aux = t[a];
        t[a] = t[b];
        t[b] = aux;
    }
}
