#!/bin/bash

APP_HOME=$(dirname "$(readlink -f "$0")")
OPTIONS="-Xlog:gc::time,pid -Xmx128m -Xms128m"
MAIN="com.threadcat.latency.mspin.MemEchoClient"
PARAMS="/tmp 0x8 1000000 1000000"

java -cp "${APP_HOME}/*" ${OPTIONS} ${MAIN} ${PARAMS}
