### WatchService vs Sockets latency

WatchService allows to use file system for message exchange without spinning.
WatchService and FileChannel can be seen in a way like Selector and SocketChannel.

This code implements two similar client-server pairs communicating through file system and sockets.
Client pings server with incremental request id waiting for response with that id and server timestamp.

CPU isolation required. Timing example:
~~~
WatchService:
Executed 100000 pings in 3.189 seconds, one-way max latency 35.021 µs, average 15.912 µs

Sockets:
Executed 100000 pings in 1.909 seconds, one-way max latency 37.831 µs, average 9.505 µs
~~~

Running without 'isolcpus' demonstrates dramatic jitter (max vs avg).
The most probable timing:
~~~
WatchService:
Executed 100000 times in 5.223 seconds, one-way max latency 10.072 millis, average 25.876 micros

Sockets:
Executed 100000 times in 2.548 seconds, one-way max latency 13.651 millis, average 12.559 micros
~~~

Environment:
~~~~
java -version
openjdk version "13.0.1" 2019-10-15
OpenJDK Runtime Environment (build 13.0.1+9)
OpenJDK 64-Bit Server VM (build 13.0.1+9, mixed mode, sharing)

uname -a
Linux 5.5.13-200.fc31.x86_64 #1 SMP Wed Mar 25 21:55:30 UTC 2020 x86_64 x86_64 x86_64 GNU/Linux

cat /proc/cpuinfo
Intel(R) Core(TM) i5-2500K CPU @ 3.30GHz
~~~~

Build instructions:
~~~
gradle clean
gradle build
gradle assembleDist
~~~
