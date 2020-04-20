#!/bin/bash

APP_HOME=$(dirname "$(readlink -f "$0")")
OPTIONS="-Xlog:gc::time,pid -Xmx128m -Xms128m -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1MaxNewSizePercent=80"
MAIN="com.threadcat.latency.watcher.WatcherEchoServer"
PARAMS="/tmp 0x4"

java -cp "${APP_HOME}/*" ${OPTIONS} ${MAIN} ${PARAMS}
