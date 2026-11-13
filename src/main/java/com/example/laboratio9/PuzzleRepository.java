package com.example.laboratio9;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Acceso a la tabla stf_puzzle_sv (una sola fila, dos columnas, sin id).
 * Como la tabla no tiene llave primaria se usa JdbcTemplate en vez de JPA.
 * Los UPDATE no llevan WHERE porque la tabla solo tiene una fila.
 */
@Repository
public class PuzzleRepository {

    private final JdbcTemplate jdbc;

    public PuzzleRepository(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public String obtenerEstructura() {
        List<String> filas = jdbc.queryForList(
                "SELECT stf_game_board_structure FROM stf_puzzle_sv LIMIT 1", String.class);
        return filas.isEmpty() ? null : filas.get(0);
    }

    public byte[] obtenerImagen() {
        List<byte[]> filas = jdbc.query(
                "SELECT image FROM stf_puzzle_sv LIMIT 1",
                (rs, i) -> rs.getBytes("image"));
        return filas.isEmpty() ? null : filas.get(0);
    }

    public boolean tieneImagen() {
        Integer n = jdbc.queryForObject(
                "SELECT COUNT(*) FROM stf_puzzle_sv WHERE image IS NOT NULL", Integer.class);
        return n != null && n > 0;
    }

    public void guardarEstructura(String estructura) {
        asegurarFila();
        jdbc.update("UPDATE stf_puzzle_sv SET stf_game_board_structure = ?", estructura);
    }

    public void guardarImagen(byte[] imagen) {
        asegurarFila();
        jdbc.update("UPDATE stf_puzzle_sv SET image = ?", (Object) imagen);
    }

    /** Nuevo juego a nivel de BD: se limpia la estructura y la imagen. */
    public void reiniciar() {
        asegurarFila();
        jdbc.update("UPDATE stf_puzzle_sv SET stf_game_board_structure = NULL, image = NULL");
    }

    private void asegurarFila() {
        Integer n = jdbc.queryForObject("SELECT COUNT(*) FROM stf_puzzle_sv", Integer.class);
        if (n == null || n == 0) {
            jdbc.update("INSERT INTO stf_puzzle_sv (stf_game_board_structure, image) VALUES (NULL, NULL)");
        }
    }
}
