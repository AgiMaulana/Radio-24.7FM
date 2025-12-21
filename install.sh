#!/bin/bash

set -e

if [[ $# -ne 2 ]]; then
  echo "Usage: curl -s https://raw.githubusercontent.com/AgiMaulana/Android-Boilerplate/agi/install-script/install.sh | bash -s -- \"ProjectName\" \"com.example.myapp\""
  exit 1
fi

PROJECT_NAME="$1"
PACKAGE_NAME="$2"
OLD_PACKAGE="io.github.agimaulana.boilerplate"
REPO_URL="https://github.com/AgiMaulana/Android-Boilerplate.git"
TEMP_DIR=$(mktemp -d)

echo "Cloning boilerplate..."
git clone --depth 1 --branch agi/install-script "$REPO_URL" "$TEMP_DIR" > /dev/null 2>&1

echo "Creating new project: $PROJECT_NAME"
cp -r "$TEMP_DIR" "./$PROJECT_NAME"
cd "./$PROJECT_NAME"

echo "Removing git history..."
rm -rf .git

# Convert packages to path segments (reversed domain style)
OLD_SEGMENTS=(${OLD_PACKAGE//./ })
NEW_SEGMENTS=(${PACKAGE_NAME//./ })

# Reverse arrays to build path from root (e.g., io -> com)
OLD_PATH=$(printf "/%s" "${OLD_SEGMENTS[@]}")
NEW_PATH=$(printf "/%s" "${NEW_SEGMENTS[@]}")

echo "Renaming package directories: $OLD_PACKAGE -> $PACKAGE_NAME"

# List of all modules from your settings.gradle.kts
MODULES=("app" "feature/sample" "core/common" "core/design" "core/network" "core/network/test" "core/shared-test" "domain/api" "domain/impl" "infrastructure")

for MODULE in "${MODULES[@]}"; do
  for SOURCE_SET in src/*/kotlin src/*/java; do  # Covers main, test, androidTest, etc.
    FULL_OLD="$MODULE/$SOURCE_SET$OLD_PATH"
    FULL_NEW="$MODULE/$SOURCE_SET$NEW_PATH"

    if [[ -d "$FULL_OLD" ]]; then
      echo "  Renaming $FULL_OLD -> $FULL_NEW"
      mkdir -p "$(dirname "$FULL_NEW")"
      mv "$FULL_OLD" "$FULL_NEW"
    fi
  done
done

# Now update package declarations in .kt/.java files and config files (safe, targeted)
find . -type f \( -name "*.kt" -o -name "*.java" \) -exec sed -i.bak "s|package $OLD_PACKAGE|package $PACKAGE_NAME|g" {} \;
find . -type f \( -name "build.gradle.kts" -o -name "AndroidManifest.xml" \) -exec sed -i.bak "s|$OLD_PACKAGE|$PACKAGE_NAME|g" {} \;
find . -type f -name "*.bak" -delete

echo "Cleanup..."
cd ..
rm -rf "$TEMP_DIR"

echo ""
echo "Done! Project '$PROJECT_NAME' created with package '$PACKAGE_NAME'."
echo "Directory structure now uses proper reversed domains if source folders existed."
echo "Open in Android Studio to verify/sync."