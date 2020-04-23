### WatchService vs Sockets latency

WatchService allows to use file system for message exchange without spinning.
WatchService and FileChannel can be seen in a way like Selector and SocketChannel.

This code implements two similar client-server pairs communicating through file system and sockets.
Client pings server with incremental request id waiting for response with that id and server timestamp.

CPU isolation required. Timing example:
~~~
WatchService:
Executed 100000 pings in 2.948 seconds, one-way max latency 49.681 µs, average 14.680 µs

Sockets:
Executed 100000 pings in 1.881 seconds, one-way max latency 41.073 µs, average 9.371 µs
~~~

Running without 'isolcpus' demonstrates dramatic jitter (max vs avg). The most probable timing:
<details>
<summary>&lt; click to expand &gt;</summary>

~~~
WatchService:
Executed 100000 times in 5.223 seconds, one-way max latency 10.072 millis, average 25.876 micros

Sockets:
Executed 100000 times in 2.548 seconds, one-way max latency 13.651 millis, average 12.559 micros
~~~
</details>

Busy-wait alternative can be applied to file and memory mapped file. Spin based ping timing:
<details>
<summary>&lt; click to expand &gt;</summary>

~~~
Spinning without mapping file to memory:
Executed 1000000 pings in 3.952 seconds, one-way max latency 31.684 µs, average 1.962 µs

Spinning memory mapped file:
Executed 1000000 pings in 0.182 seconds, one-way max latency 35.742 µs, average 0.071 µs
~~~
</details>

Environment:
~~~~
java -version
openjdk version "14.0.1" 2020-04-14
OpenJDK Runtime Environment (build 14.0.1+7)
OpenJDK 64-Bit Server VM (build 14.0.1+7, mixed mode, sharing)

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

Notes and considerations:

[**Wiki**](../../wiki/Notes)