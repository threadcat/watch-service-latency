#!/bin/bash

APP_HOME=$(dirname "$(readlink -f "$0")")
OPTIONS="-Xlog:gc::time,pid -Xmx128m -Xms128m"
MAIN="com.threadcat.latency.fspin.SpinningEchoServer"
PARAMS="/tmp 0x4"

java -cp "${APP_HOME}/*" ${OPTIONS} ${MAIN} ${PARAMS}
