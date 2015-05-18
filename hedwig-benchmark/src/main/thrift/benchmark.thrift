/*
* benchmark.thrift
* benchmark hedwig service
*/
namespace cpp me.ellios.hedwig.benchmark.thrift
namespace java  me.ellios.hedwig.benchmark.thrift
namespace py me.ellios.hedwig.benchmark.thrift

service Benchmark {

   string ping(1:string ping),

   binary benchmark(1:binary data),

}