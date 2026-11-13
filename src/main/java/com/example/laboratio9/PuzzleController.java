package com.example.laboratio9;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * API que usa la pagina (fetch) para que todo funcione sin recargar.
 */
@RestController
@RequestMapping("/api")
public class PuzzleController {

    private final PuzzleRepository puzzleRepository;

    public PuzzleController(PuzzleRepository puzzleRepository) {
        this.puzzleRepository = puzzleRepository;
    }

    /** Estado actual: sirve para retomar el juego si se cerro la web. */
    @GetMapping("/estado")
    public Map<String, Object> estado() {
        String[] tablero = Tablero.desdeTexto(puzzleRepository.obtenerEstructura());
        return respuesta(tablero, false);
    }

    /** Devuelve la imagen guardada en la BD (404 si aun no se subio ninguna). */
    @GetMapping("/imagen")
    public ResponseEntity<byte[]> imagen() {
        byte[] img = puzzleRepository.obtenerImagen();
        if (img == null || img.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .header("Cache-Control", "no-store")
                .body(img);
    }

    /** Sube la imagen del personaje. Solo se permite formato JPG. */
    @PostMapping("/imagen")
    public ResponseEntity<Map<String, Object>> subirImagen(@RequestParam("archivo") MultipartFile archivo)
            throws IOException {
        if (archivo == null || archivo.isEmpty()) {
            return error("Debe seleccionar una imagen.");
        }
        String nombre = archivo.getOriginalFilename() == null ? "" : archivo.getOriginalFilename().toLowerCase();
        byte[] bytes = archivo.getBytes();
        boolean extensionJpg = nombre.endsWith(".jpg") || nombre.endsWith(".jpeg");
        // Los archivos JPG empiezan con los bytes FF D8 FF
        boolean contenidoJpg = bytes.length > 3
                && (bytes[0] & 0xFF) == 0xFF && (bytes[1] & 0xFF) == 0xD8 && (bytes[2] & 0xFF) == 0xFF;
        if (!extensionJpg || !contenidoJpg) {
            return error("Solo se permiten imágenes en formato JPG.");
        }
        if (Tablero.desdeTexto(puzzleRepository.obtenerEstructura()) != null) {
            return error("Hay un juego en curso; termínelo antes de cambiar la imagen.");
        }
        puzzleRepository.guardarImagen(bytes);
        Map<String, Object> r = new HashMap<>();
        r.put("ok", true);
        return ResponseEntity.ok(r);
    }

    /** Inicia el juego: valida "NxN", desordena con la secuencia fija y guarda en la BD. */
    @PostMapping("/juego/iniciar")
    public ResponseEntity<Map<String, Object>> iniciar(@RequestParam("dimension") String dimension) {
        int n = Tablero.leerDimension(dimension);
        if (n == -1) {
            return error("Ingrese las dimensiones en el formato NxN (ej. 3x3), con N entre "
                    + Tablero.MIN_N + " y " + Tablero.MAX_N + ".");
        }
        String[] tablero = Tablero.desordenar(n);
        puzzleRepository.guardarEstructura(Tablero.aTexto(tablero));
        return ResponseEntity.ok(respuesta(tablero, false));
    }

    /** Mueve la figura de la casilla indicada (indice 0..N*N-1) al espacio vacio. */
    @PostMapping("/juego/mover")
    public ResponseEntity<Map<String, Object>> mover(@RequestParam("posicion") int posicion) {
        String[] tablero = Tablero.desdeTexto(puzzleRepository.obtenerEstructura());
        if (tablero == null) {
            return error("No hay un juego en curso.");
        }
        if (!Tablero.mover(tablero, posicion)) {
            return error("Movimiento no válido: la casilla no es contigua al espacio vacío.");
        }
        if (Tablero.estaResuelto(tablero)) {
            // Juego terminado -> nuevo juego en la BD
            puzzleRepository.reiniciar();
            return ResponseEntity.ok(respuesta(tablero, true));
        }
        puzzleRepository.guardarEstructura(Tablero.aTexto(tablero));
        return ResponseEntity.ok(respuesta(tablero, false));
    }

    private Map<String, Object> respuesta(String[] tablero, boolean terminado) {
        Map<String, Object> r = new HashMap<>();
        r.put("tieneImagen", !terminado && puzzleRepository.tieneImagen());
        r.put("terminado", terminado);
        if (tablero != null) {
            r.put("n", Tablero.dimension(tablero));
            r.put("tablero", tablero);
            r.put("estructura", Tablero.aTexto(tablero));
        }
        return r;
    }

    private ResponseEntity<Map<String, Object>> error(String mensaje) {
        Map<String, Object> r = new HashMap<>();
        r.put("error", mensaje);
        return ResponseEntity.badRequest().body(r);
    }
}
