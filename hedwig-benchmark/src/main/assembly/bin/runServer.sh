#!/bin/sh

##ulimit -n 65535
##ulimit -s 1024

PORT=$1

JAVA_OPTS="-server -d64 -Xms2048m -Xmx2048m -Xmn1024m -XX:MaxPermSize=128m \
-Xss512k -XX:+UseConcMarkSweepGC \
-XX:+UseParNewGC -XX:CMSFullGCsBeforeCompaction=5 \
-XX:+UseCMSCompactAtFullCollection \
-XX:+PrintGC -Xloggc:/data/vrslogs/hedwig/benchmark_server_gc.log -Dport=${PORT}"

LOG="/data/vrslogs/hedwig/benchmark_server_std.log"

PHOME=$(dirname `readlink -f "$0"`)
PHOME=$(dirname ${PHOME})
java ${JAVA_OPTS} -cp ${PHOME}/conf:${PHOME}/lib/* com.qiyi.vrs.hedwig.benchmark.BenchmarkServer 1> ${LOG} 2>&1 &