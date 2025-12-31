#!/bin/bash
#
# Script de génération des classes Java depuis les JSON Schemas
#
# Ce script:
# 1. Génère les JSON Schemas depuis les schémas Zod
# 2. Appelle Maven pour générer les classes Java via jsonschema2pojo
#
# Usage: ./scripts/generate-java.sh
#

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_DIR="$(dirname "$SCRIPT_DIR")"

echo "============================================================"
echo "COMPTA Notification Contracts - Java Generation"
echo "============================================================"

# Aller dans le répertoire du projet
cd "$PROJECT_DIR"

# Étape 1: Générer les JSON Schemas depuis Zod
echo ""
echo "Step 1: Generating JSON Schemas from Zod..."
echo "------------------------------------------------------------"

if [ ! -d "node_modules" ]; then
    echo "Installing npm dependencies..."
    npm install
fi

npm run generate:schemas

# Vérifier que les schemas ont été générés
if [ ! -d "generated/json-schemas" ]; then
    echo "ERROR: JSON Schemas not generated!"
    exit 1
fi

echo ""
echo "JSON Schemas generated successfully!"
ls -la generated/json-schemas/

# Étape 2: Générer les classes Java via Maven
echo ""
echo "Step 2: Generating Java classes from JSON Schemas..."
echo "------------------------------------------------------------"

# Vérifier si Maven est disponible
if command -v mvn &> /dev/null; then
    mvn generate-sources -DskipTests
    echo ""
    echo "Java classes generated successfully!"
elif command -v ./mvnw &> /dev/null; then
    ./mvnw generate-sources -DskipTests
    echo ""
    echo "Java classes generated successfully!"
else
    echo "WARNING: Maven not found. Run 'mvn generate-sources' manually."
fi

echo ""
echo "============================================================"
echo "Generation complete!"
echo "============================================================"
echo ""
echo "Generated files:"
echo "  - JSON Schemas: generated/json-schemas/*.schema.json"
echo "  - Java classes: target/generated-sources/jsonschema2pojo/"
echo ""
