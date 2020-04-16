#!/bin/bash

APP_HOME=$(dirname "$(readlink -f "$0")")
OPTIONS="-Xlog:gc::time,pid -Xmx512m -Xms512m"
MAIN="com.threadcat.latency.socket.SocketEchoServer"
PARAMS="localhost 11000 0x4"

java -cp "${APP_HOME}/*" ${OPTIONS} ${MAIN} ${PARAMS}
