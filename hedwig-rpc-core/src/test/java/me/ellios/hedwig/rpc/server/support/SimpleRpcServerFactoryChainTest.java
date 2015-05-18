package me.ellios.hedwig.rpc.server.support;

import me.ellios.hedwig.rpc.core.ServiceConfig;
import me.ellios.hedwig.rpc.core.ServiceSchema;
import me.ellios.hedwig.rpc.core.ServiceType;
import me.ellios.hedwig.rpc.server.RpcServer;
import me.ellios.hedwig.rpc.server.RpcServerFactory;
import me.ellios.hedwig.rpc.server.RpcServerFactoryChain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.*;

/**
 * Say something?
 *
 * @author George Cao
 * @since 4/28/13 3:58 PM
 */
public class SimpleRpcServerFactoryChainTest {
    private static final Logger LOG = LoggerFactory.getLogger(SimpleRpcServerFactoryChainTest.class);
    RpcServerFactoryChain chain;

    @BeforeMethod
    public void setUp() throws Exception {
        chain = new SimpleRpcServerFactoryChain(new RpcServerFactory() {
            @Override
            public boolean accept(ServiceSchema schema, ServiceType type) {
                return false;
            }

            @Override
            public RpcServer create() {
                return null;
            }
        });
    }

    @Test
    public void testHasNext() throws Exception {
        assertTrue(!chain.hasNext());
        chain.addFactory(new RpcServerFactory() {
            @Override
            public boolean accept(ServiceSchema schema, ServiceType type) {
                return false;
            }

            @Override
            public RpcServer create() {
                return null;
            }
        });
        assertTrue(chain.hasNext());
    }

    private List<RpcServerFactory> create(int count) {
        List<RpcServerFactory> list = new ArrayList<>(count + 1);
        list.add(chain.getFactory());
        for (int i = 0; i < count; i++) {
            RpcServerFactory factory = new RpcServerFactory() {
                @Override
                public boolean accept(ServiceSchema schema, ServiceType type) {
                    return false;
                }

                @Override
                public RpcServer create() {
                    return null;
                }
            };
            chain.addFactory(factory);
            list.add(factory);
        }
        return list;
    }


    @Test
    public void testGetNext() throws Exception {
        int round = 100;
        List<RpcServerFactory> list = create(round);
        RpcServerFactoryChain current = chain;
        for (int i = 0; i <= round; i++) {
            assertTrue(current.getFactory() == list.get(i));
            current = current.getNext();
        }
    }

    @Test
    public void testAddNext() throws Exception {
        SimpleRpcServerFactoryChain c = new SimpleRpcServerFactoryChain(new RpcServerFactory() {
            @Override
            public boolean accept(ServiceSchema schema, ServiceType type) {
                return false;
            }

            @Override
            public RpcServer create() {
                return null;
            }
        });
        chain.addNext(c);
        RpcServerFactoryChain c1 = new SimpleRpcServerFactoryChain(new RpcServerFactory() {
            @Override
            public boolean accept(ServiceSchema schema, ServiceType type) {
                return false;
            }

            @Override
            public RpcServer create() {
                return null;
            }
        });
        chain.addNext(c1);
        assertTrue(c == chain.getNext());
        assertTrue(c1 == chain.getNext().getNext());
    }

    @Test
    public void testAddFactory() throws Exception {
        RpcServerFactory factory = new RpcServerFactory() {
            @Override
            public boolean accept(ServiceSchema schema, ServiceType type) {
                return false;
            }

            @Override
            public RpcServer create() {
                return null;
            }
        };
        chain.addFactory(factory);
        assertTrue(chain.getNext().getFactory() == factory);
    }

    @Test
    public void testCreate() throws Exception {
        final RpcServer server = new RpcServer() {
            @Override
            public void start() {
            }

            @Override
            public void stop() {
            }

            @Override
            public void registerService(ServiceConfig serviceConfig) {
            }
        };
        chain.addFactory(new RpcServerFactory() {
            @Override
            public boolean accept(ServiceSchema schema, ServiceType type) {
                return schema == ServiceSchema.TCP;
            }

            @Override
            public RpcServer create() {
                return server;
            }
        });
        chain.addFactory(new RpcServerFactory() {
            @Override
            public boolean accept(ServiceSchema schema, ServiceType type) {
                return schema == ServiceSchema.SPDY && type == ServiceType.THRIFT;
            }

            @Override
            public RpcServer create() {
                return server;
            }
        });
        assertNotNull(chain.create(ServiceSchema.TCP, ServiceType.THRIFT));
        assertNull(chain.create(ServiceSchema.HTTP, ServiceType.THRIFT));
        assertNull(chain.create(ServiceSchema.HTTP, ServiceType.PROTOBUF));
        assertNotNull(chain.create(ServiceSchema.SPDY, ServiceType.THRIFT));
        assertNull(chain.create(ServiceSchema.SPDY, ServiceType.PROTOBUF));
    }
}
