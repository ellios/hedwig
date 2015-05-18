namespace java me.ellios.hedwig.http.service

include "payload.thrift"

service TRestService {
    i64 mixin(1: i64 p1, 2: payload.TPayload p2, 3: i32 id);
    i64 create(1: i64 p1, 2: payload.TPayload p2);
    i64 multiPost(1: payload.TPayload p1, 2: payload.TPayload p2);
    i64 post(1: payload.TPayload payload);
    i64 batch(1: list<payload.TPayload> payloads);
    payload.TPayload fetch(1: i64 id);
    list<payload.TPayload> multiGet(1: list<i64> idList);
    map<i64,payload.TPayload> fetchMap(1: i64 id);
    set<payload.TPayload> fetchSet(1: i64 id);
}
