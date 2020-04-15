#!/bin/bash

CLASSPATH=../../../out/production/classes

java -cp $CLASSPATH -Xlog:gc*::time,pid -Xmx1g -Xms1g latency.socket.SocketEchoServer

