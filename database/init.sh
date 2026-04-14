#!/bin/bash
set -e
# Run image.sql against the already-created 'image' database, skipping the CREATE DATABASE line
grep -v "CREATE DATABASE" /docker-entrypoint-initdb.d/image.sql \
  | psql -v ON_ERROR_STOP=1 -U "$POSTGRES_USER" -d "$POSTGRES_DB"
