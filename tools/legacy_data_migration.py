#!/usr/bin/env python3
"""
Convierte el backup legacy de diccionarios y cartas (versión 0.1.0, en CSV) al esquema V3.

Genera un script SQL de un solo uso: no es una migración de Flyway, se lanza a mano contra el
entorno que se quiera sembrar, después del baseline.

    python3 legacy_data_migration.py dictionaries.csv cards.csv --engine mariadb -o legacy-data.sql

Qué hace, y por qué:

* **Solo migra los diccionarios que tienen cartas.** Los 61 restantes son fichas creadas y
  abandonadas sin contenido.
* **Sintetiza los usuarios.** Del backup de usuarios solo sobrevivió el `creator_id`, que es el id de
  Telegram. Se crea un `users` con `username = "tg:<id>"` y su fila en `telegram_user`, de modo que
  el `login()` del bot les ponga su alias y su nombre real la primera vez que vuelvan a escribir.
* **UUID deterministas** derivados del id antiguo: se puede rastrear qué fila era qué y permite
  fijar el diccionario por defecto en la configuración de la aplicación.
* **Relanzable**: los INSERT se saltan las filas que ya estén (INSERT IGNORE en MariaDB, NOT EXISTS
  en H2), así que volver a lanzarlo no duplica nada ni aborta a mitad.
* **Los colaboradores se pierden**: no había CSV de `dictionary_collaborator` en el backup.
"""

import argparse
import csv
import sys
from datetime import datetime

# Namespaces de los UUID deterministas: un "nodo" distinto por tipo de entidad para que los ids
# antiguos, que se solapan entre tablas, no colisionen entre sí.
NS_DICTIONARY = "a000"
NS_USER = "a001"
NS_CARD = "a002"

# El backup no trae fecha en 60 diccionarios ni en 6.786 cartas. Se les pone la del arranque del
# bot original, que es anterior a cualquier dato real, para que el orden cronológico no mienta.
FALLBACK_DATE = "2020-01-01 00:00:00"

# CardTypeEnum es @Enumerated(ORDINAL): BLACK=0, WHITE=1. El backup usa 1=blanca, 2=negra.
LEGACY_CARD_TYPE = {"1": 1, "2": 0}

DEFAULT_LANG = "es"


def deterministic_uuid(namespace, legacy_id):
    return f"00000000-0000-4000-{namespace}-{int(legacy_id):012x}"


def parse_date(value):
    """El backup guarda las fechas en formato asctime: 'Sat Apr 25 20:47:41 2020'."""
    if not value or not value.strip():
        return FALLBACK_DATE
    try:
        return datetime.strptime(value.strip(), "%a %b %d %H:%M:%S %Y").strftime("%Y-%m-%d %H:%M:%S")
    except ValueError:
        return FALLBACK_DATE


def quote(text):
    return "'" + text.replace("\\", "\\\\").replace("'", "''") + "'"


def uuid_literal(value):
    # Los dos motores usan tipo uuid nativo, asi que vale el literal canonico con guiones:
    # H2 lo tiene de siempre y MariaDB desde la 10.7, que es la version para la que se genera
    # el baseline. Antes MariaDB llevaba binary(16) y aqui habia que envolverlo en UNHEX().
    return f"'{value}'"


def bool_literal(value):
    return "1" if value in ("1", 1, True) else "0"


def write_insert(w, engine, table, columns, rows, pk="id"):
    """Escribe un INSERT que se salta las filas que ya existan.

    Hace falta porque el script se puede tener que relanzar (un lote que falló a medias, un backup
    corregido) y porque el bot ya está en marcha: si uno de los creadores del backup ya ha escrito
    al bot nuevo, su fila de telegram_user existe y un INSERT normal aborta la transacción entera.

    Los dos motores lo escriben distinto. MariaDB tiene INSERT IGNORE; H2 no lo admite, ni tampoco
    ON CONFLICT DO NOTHING, así que ahí se filtra con un NOT EXISTS sobre un VALUES derivado, que
    conserva el insertado por lotes.

    Ojo con INSERT IGNORE: en MariaDB degrada a aviso *cualquier* error de fila, no solo la clave
    duplicada (truncados, fechas inválidas, claves ajenas). Por eso main() valida antes el largo de
    los textos y los tipos de carta, y descarta las cartas huérfanas: cuando llega aquí, lo único
    que debería poder saltar es una clave repetida.
    """
    cols = ", ".join(columns)

    if engine == "mariadb":
        w(f"INSERT IGNORE INTO {table} ({cols}) VALUES\n")
        w(",\n".join(rows) + ";\n\n")
    else:
        projection = ", ".join(f"v.{c}" for c in columns)
        w(f"INSERT INTO {table} ({cols})\nSELECT {projection} FROM (VALUES\n")
        w(",\n".join(rows) + f"\n) v({cols})\nWHERE NOT EXISTS (SELECT 1 FROM {table} WHERE {table}.{pk} = v.{pk});\n\n")


def chunked(items, size):
    for i in range(0, len(items), size):
        yield items[i:i + size]


def main():
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    parser.add_argument("dictionaries_csv")
    parser.add_argument("cards_csv")
    parser.add_argument("--engine", choices=("mariadb", "h2"), default="mariadb")
    parser.add_argument("-o", "--output", default="-")
    parser.add_argument("--batch-size", type=int, default=500)
    args = parser.parse_args()

    with open(args.dictionaries_csv, encoding="utf-8") as f:
        dictionaries = [row for row in csv.DictReader(f) if row.get("id")]
    with open(args.cards_csv, encoding="utf-8") as f:
        cards = [row for row in csv.DictReader(f) if row.get("id")]

    known_dictionary_ids = {d["id"] for d in dictionaries}

    orphans = [c for c in cards if c["dictionary_id"] not in known_dictionary_ids]
    cards = [c for c in cards if c["dictionary_id"] in known_dictionary_ids]

    unknown_types = {c["type"] for c in cards} - set(LEGACY_CARD_TYPE)
    if unknown_types:
        sys.exit(f"Tipos de carta desconocidos en el backup: {sorted(unknown_types)}")

    too_long = [c for c in cards if len(c["text"]) > 256]
    if too_long:
        sys.exit(f"{len(too_long)} cartas superan los 256 caracteres de la columna")

    cards_per_dictionary = {}
    for card in cards:
        cards_per_dictionary.setdefault(card["dictionary_id"], []).append(card)

    # Solo los diccionarios con contenido
    dictionaries = [d for d in dictionaries if cards_per_dictionary.get(d["id"])]
    kept_ids = {d["id"] for d in dictionaries}
    cards = [c for c in cards if c["dictionary_id"] in kept_ids]

    creators = sorted({int(d["creator_id"]) for d in dictionaries})

    out = open(args.output, "w", encoding="utf-8") if args.output != "-" else sys.stdout
    try:
        write(out, args, dictionaries, cards, creators, orphans)
    finally:
        if out is not sys.stdout:
            out.close()
            print(f"Escrito {args.output}: {len(creators)} usuarios, {len(dictionaries)} diccionarios, "
                  f"{len(cards)} cartas.", file=sys.stderr)


def write(out, args, dictionaries, cards, creators, orphans):
    engine = args.engine
    w = out.write

    w(f"""-- Datos legacy del backup 0.1.0 convertidos al esquema V3 ({engine}).
--
-- Generado con CAH-Telegram/tools/legacy_data_migration.py. Script de un solo uso: se ejecuta a
-- mano DESPUÉS del baseline de Flyway, no forma parte del histórico de migraciones.
--
--   usuarios sintetizados : {len(creators)}
--   diccionarios          : {len(dictionaries)}
--   cartas                : {len(cards)}
--   cartas descartadas    : {len(orphans)} (apuntaban a un diccionario inexistente)
--
-- Los usuarios se crean con username "tg:<id de telegram>" porque el backup de usuarios se perdió.
-- No es un apaño permanente: en cuanto cada persona vuelva a escribir al bot, el login le pone su
-- alias real y su nombre visible.

{"START TRANSACTION;" if engine == "mariadb" else "SET AUTOCOMMIT FALSE;"}

""")

    w("-- Usuarios sintetizados a partir de los creadores de los diccionarios\n")
    rows = []
    for telegram_id in creators:
        user_uuid = uuid_literal(deterministic_uuid(NS_USER, telegram_id))
        synthetic = f"tg:{telegram_id}"
        rows.append(f"    ({user_uuid}, '{FALLBACK_DATE}', 1, {quote(synthetic)}, {quote(synthetic)}, '{DEFAULT_LANG}')")
    write_insert(w, engine, "users", ["id", "creation_date", "active", "name", "username", "lang_id"], rows)

    w("-- Equivalencia con Telegram: el id de usuario del backup ya era el id de Telegram\n")
    rows = []
    for telegram_id in creators:
        user_uuid = uuid_literal(deterministic_uuid(NS_USER, telegram_id))
        rows.append(f"    ({telegram_id}, {user_uuid}, '{DEFAULT_LANG}', NULL)")
    write_insert(w, engine, "telegram_user", ["id", "user_id", "language_code", "last_seen"], rows)

    w("-- Diccionarios (solo los que tienen cartas)\n")
    rows = []
    for d in dictionaries:
        rows.append(
            f"    ({uuid_literal(deterministic_uuid(NS_DICTIONARY, d['id']))}, "
            f"'{parse_date(d['creation_date'])}', {quote(d['name'])}, "
            f"{bool_literal(d['published'])}, {bool_literal(d['shared'])}, "
            f"{uuid_literal(deterministic_uuid(NS_USER, d['creator_id']))}, '{DEFAULT_LANG}')")
    write_insert(w, engine, "dictionary", ["id", "creation_date", "name", "published", "shared", "creator_id", "lang_id"], rows)

    w(f"-- Cartas ({len(cards)}), en lotes de {args.batch_size}\n")
    for batch in chunked(cards, args.batch_size):
        rows = []
        for c in batch:
            rows.append(
                f"    ({uuid_literal(deterministic_uuid(NS_CARD, c['id']))}, "
                f"'{parse_date(c['creation_date'])}', {quote(c['text'])}, "
                f"{LEGACY_CARD_TYPE[c['type']]}, "
                f"{uuid_literal(deterministic_uuid(NS_DICTIONARY, c['dictionary_id']))})")
        write_insert(w, engine, "card", ["id", "creation_date", "text", "type", "dictionary_id"], rows)

    w("COMMIT;\n")


if __name__ == "__main__":
    main()
