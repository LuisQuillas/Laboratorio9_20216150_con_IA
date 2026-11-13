-- Script manual (opcional: la app ya crea la BD, la tabla y la fila al iniciar)
CREATE DATABASE IF NOT EXISTS stf_games_20216150;
USE stf_games_20216150;

CREATE TABLE IF NOT EXISTS stf_puzzle_sv (
    stf_game_board_structure VARCHAR(500) NULL,
    image LONGBLOB NULL
);

INSERT INTO stf_puzzle_sv (stf_game_board_structure, image)
SELECT NULL, NULL FROM DUAL
WHERE NOT EXISTS (SELECT 1 FROM stf_puzzle_sv);
