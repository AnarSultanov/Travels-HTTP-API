#!/bin/sh
echo "Unzipping data..."
mkdir -p /data/
unzip -o /tmp/data/data.zip -d /data/
echo "Starting server..."
java -server -Xms3200m -Xmx3200m -XX:+AggressiveOpts -XX:+UseG1GC -XX:MaxGCPauseMillis=10 -Djava.security.egd=file:/dev/./urandom -jar app.jar prod