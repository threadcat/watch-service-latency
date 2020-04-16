#!/bin/bash

APP_HOME=$(dirname "$(readlink -f "$0")")
X_OPTIONS="-Xlog:gc::time,pid -Xmx512m -Xms512m"

java -cp "${APP_HOME}/*" ${X_OPTIONS} com.threadcat.latency.watcher.WatcherEchoClient /tmp 0x8
