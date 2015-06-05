#!/bin/sh

##ulimit -n 65535
##ulimit -s 1024

JAVA_OPTS="-server -d64 -Xms2048m -Xmx2048m -XX:MaxPermSize=128m \
-Xss512k -XX:+UseConcMarkSweepGC \
-XX:+UseParNewGC -XX:CMSFullGCsBeforeCompaction=5 \
-XX:+UseCMSCompactAtFullCollection \
-XX:+PrintGC -Xloggc:/data/vrslogs/hedwig/benchmark_client_gc.log"

PHOME=$(dirname `readlink -f "$0"`)
PHOME=$(dirname $PHOME)
type=$1
concurent=$2
time=$3

THRIFT_LOG="/data/vrslogs/hedwig/benchmark_client_thrift.log"
OIO_LOG="/data/vrslogs/hedwig/benchmark_client_oio.log"
PB_LOG="/data/vrslogs/hedwig/benchmark_client_pb.log"

if [ "${type}" == "thrift" ]; then
    java ${JAVA_OPTS} -cp ${PHOME}/conf:${PHOME}/lib/* -Dlog.path=${THRIFT_LOG} me.ellios.hedwig.benchmark.thrift.client.ThriftBenchmarkClient 1> ${THRIFT_LOG} 2>&1 &
elif [ "${type}" == "oio" ]; then
    java ${JAVA_OPTS} -cp ${PHOME}/conf:${PHOME}/lib/* -Dlog.path=${OIO_LOG} me.ellios.hedwig.benchmark.thrift.client.OioThriftBenchmarkClient $2 1> ${OIO_LOG} 2>&1 &
else
    java ${JAVA_OPTS} -cp ${PHOME}/conf:${PHOME}/lib/* -Dlog.path=${PB_LOG} me.ellios.hedwig.benchmark.pb.client.PbBenchmarkClient 1> ${PB_LOG} 2>&1 &
fi