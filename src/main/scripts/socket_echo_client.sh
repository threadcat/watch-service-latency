#!/bin/bash

APP_HOME=$(dirname "$(readlink -f "$0")")
OPTIONS="-Xlog:gc::time,pid -Xmx128m -Xms128m -XX:+UnlockExperimentalVMOptions -XX:+UseG1GC -XX:G1NewSizePercent=60"
MAIN="com.threadcat.latency.socket.SocketEchoClient"
PARAMS="localhost 11000 0x8 100000 100000"

java -cp "${APP_HOME}/*" ${OPTIONS} ${MAIN} ${PARAMS}
