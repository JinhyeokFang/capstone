#!/bin/bash

set -e

echo "Starting database initialization..."

for file in /docker-entrypoint-initdb.d/*.sql; do
    if [ -f "$file" ]; then
        echo "Executing: $file"
        mysql --default-character-set=utf8mb4 -uroot -p${MYSQL_ROOT_PASSWORD} < "$file"
    fi
done

echo "Database initialization completed."



