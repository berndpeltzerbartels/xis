package one.xis.mongodb;

import com.mongodb.client.MongoDatabase;
import lombok.RequiredArgsConstructor;
import one.xis.context.Component;
import one.xis.context.ProxyFactory;

import java.lang.reflect.Proxy;

@Component
@RequiredArgsConstructor
class MongoRepositoryProxyFactory<I> implements ProxyFactory<I> {
    private final MongoDatabase database;

    @Override
    public I createProxy(Class<I> interf) {
        var handler = new MongoRepositoryProxyMethodHandler(database, interf);
        Object proxy = Proxy.newProxyInstance(interf.getClassLoader(), new Class[]{interf}, handler);
        return interf.cast(proxy);
    }
}
