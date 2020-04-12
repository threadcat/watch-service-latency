##### Testing WatchService latency

Executed 10000 times in 0.345 seconds, one-way max latency 1.594 millis, average 17.139 micros

No explanation yet for dramatic 'max latency'.

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