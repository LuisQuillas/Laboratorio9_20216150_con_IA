-- Garantiza que exista exactamente una fila (solo la inserta si la tabla esta vacia)
INSERT INTO stf_puzzle_sv (stf_game_board_structure, image)
SELECT NULL, NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM stf_puzzle_sv);
