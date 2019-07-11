#!/usr/bin/env bash
function copyEnvVarsToGradleProperties {
    GRADLE_HOME=$HOME"/.gradle"
    GRADLE_PROPERTIES=$GRADLE_HOME"/gradle.properties"
    export GRADLE_HOME
    export GRADLE_PROPERTIES
    echo "Gradle Properties should exist at $GRADLE_PROPERTIES"

    if [[ ! -f "$GRADLE_PROPERTIES" ]]; then
        echo "Gradle Properties does not exist"

        echo "Creating Gradle Properties file..."
        mkdir -p $GRADLE_HOME && touch $GRADLE_PROPERTIES

        echo "Writing ITAD_KEYS to gradle.properties..."
        echo "ITAD_CLIENT_ID=$ITAD_CLIENT_ID" >> $GRADLE_PROPERTIES
        echo "ITAD_CLIENT_SECRET=$ITAD_CLIENT_SECRET" >> $GRADLE_PROPERTIES
        echo "ITAD_API_KEY=$ITAD_API_KEY" >> $GRADLE_PROPERTIES
    fi
}
