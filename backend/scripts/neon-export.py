#!/usr/bin/env python3
"""Exporte les donnees de la base Neon vers un fichier SQL rejouable en local.

Passe par l'API SQL-sur-HTTPS de Neon (port 443) et non par le protocole
Postgres (port 5432), ce qui permet de fonctionner meme depuis un reseau qui
filtre le port 5432.

Usage: python scripts/neon-export.py [chemin/.env] [fichier/sortie.sql]
"""
import json
import os
import re
import sys
import urllib.request

BACKEND_DIR = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DEFAULT_ENV = os.path.join(BACKEND_DIR, ".env")
DEFAULT_OUT = os.path.join(BACKEND_DIR, "db-local", "neon-data.sql")


def load_env(path):
    env = {}
    with open(path, encoding="utf-8") as handle:
        for line in handle:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            key, value = line.split("=", 1)
            env[key.strip()] = value.strip()
    return env


class Neon:
    def __init__(self, env):
        url = env["NEON_DB_URL"] if "NEON_DB_URL" in env else env["DB_URL"]
        self.host = re.search(r"//([^/:?]+)", url).group(1)
        database = re.search(r"/([^/?]+)(?:\?|$)", url).group(1)
        user = env.get("NEON_DB_USERNAME", env.get("DB_USERNAME"))
        password = env.get("NEON_DB_PASSWORD", env.get("DB_PASSWORD"))
        self.conn = f"postgresql://{user}:{password}@{self.host}/{database}?sslmode=require"

    def query(self, sql):
        body = json.dumps({"query": sql, "params": []}).encode()
        request = urllib.request.Request(
            f"https://{self.host}/sql",
            data=body,
            method="POST",
            headers={
                "Neon-Connection-String": self.conn,
                "Neon-Raw-Text-Output": "true",
                "Content-Type": "application/json",
            },
        )
        with urllib.request.urlopen(request, timeout=120) as response:
            return json.loads(response.read())["rows"]


def sql_literal(text):
    return "'" + text.replace("'", "''") + "'"


def main():
    env_path = sys.argv[1] if len(sys.argv) > 1 else DEFAULT_ENV
    out_path = sys.argv[2] if len(sys.argv) > 2 else DEFAULT_OUT
    neon = Neon(load_env(env_path))

    tables = [
        row["table_name"]
        for row in neon.query(
            "select table_name from information_schema.tables "
            "where table_schema='public' and table_type='BASE TABLE' "
            "order by table_name"
        )
    ]

    lines = [
        "-- Donnees exportees depuis Neon via l'API SQL-sur-HTTPS.",
        "-- Le schema est cree par Hibernate (ddl-auto=update) : demarrer le",
        "-- backend une premiere fois avant de rejouer ce fichier.",
        "BEGIN;",
        "-- Desactive les contraintes FK le temps du chargement : l'ordre des",
        "-- tables n'a donc pas d'importance.",
        "SET session_replication_role = replica;",
        "",
    ]
    total = 0

    for table in tables:
        rows = neon.query(f'select row_to_json(t)::text as j from "{table}" t')
        lines.append(f'-- {table} ({len(rows)} lignes)')
        lines.append(f'DELETE FROM "{table}";')
        for row in rows:
            payload = sql_literal(row["j"])
            lines.append(
                f'INSERT INTO "{table}" SELECT * FROM '
                f'json_populate_record(NULL::"{table}", {payload});'
            )
        lines.append("")
        total += len(rows)
        print(f"{table:<28} {len(rows):>6} lignes")

    lines.append("SET session_replication_role = DEFAULT;")
    lines.append("")
    lines.append("-- Recale les sequences des colonnes IDENTITY sur le max() insere.")
    for table in tables:
        identity_columns = [
            row["column_name"]
            for row in neon.query(
                "select column_name from information_schema.columns "
                f"where table_schema='public' and table_name='{table}' "
                "and is_identity='YES'"
            )
        ]
        for column in identity_columns:
            lines.append(
                f"SELECT setval(pg_get_serial_sequence('\"{table}\"', '{column}'), "
                f'coalesce((SELECT max("{column}") FROM "{table}"), 1));'
            )
    lines.append("COMMIT;")
    lines.append("")

    os.makedirs(os.path.dirname(out_path), exist_ok=True)
    with open(out_path, "w", encoding="utf-8", newline="\n") as handle:
        handle.write("\n".join(lines))
    print(f"\n{total} lignes ecrites dans {out_path}")


if __name__ == "__main__":
    main()
