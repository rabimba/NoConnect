#!/bin/bash
set -e

PROJECTS=(
    'nc-api'
    'nc-core'
    'nc-android'
    'nc-java'
    'noconnect-api'
    'noconnect-core'
    'noconnect-android'
    'noconnect-headless'
)

# clear witness files to prevent errors when upgrading dependencies
for project in ${PROJECTS[@]}
do
    echo "" > ${project}/witness.gradle
done

# calculating new checksums
for project in ${PROJECTS[@]}
do
    echo "Calculating new checksums for ${project} ..."
    ./gradlew -q --configure-on-demand ${project}:calculateChecksums \
        | grep -v '^\(Skipping\|Verifying\|Welcome to Gradle\)' \
        | sed "s/    /\t/g" \
        > ${project}/witness.gradle
done
