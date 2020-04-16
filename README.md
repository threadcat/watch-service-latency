### Testing WatchService vs Sockets latency

CPU isolation required. Timing example:
~~~
WatchService:
Executed 100000 pings in 3.074 seconds, one-way max latency 130.600 µs, average 15.344 µs

Sockets:
Executed 100000 pings in 1.962 seconds, one-way max latency 37.164 µs, average 9.765 µs
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

dd bs=65536 count=163840 if=/dev/zero of=to_delete && rm to_delete
10737418240 bytes (11 GB, 10 GiB) copied, 41.6239 s, 258 MB/s
~~~~

Build instructions:
~~~
gradle clean
gradle build
gradle assembleDist
~~~