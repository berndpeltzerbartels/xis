package one.xis.sql;

import one.xis.context.Component;
import one.xis.context.ProxyFactory;

import javax.sql.DataSource;
import java.lang.reflect.Proxy;

@Component
class SQLRepositoryProxyFactory<I> implements ProxyFactory<I> {
    private final DataSource dataSource;
    private final SqlConnectionProvider connectionProvider;
    private final TransactionManager transactionManager;

    SQLRepositoryProxyFactory(DataSource dataSource, SqlConnectionProvider connectionProvider, TransactionManager transactionManager) {
        this.dataSource = dataSource;
        this.connectionProvider = connectionProvider;
        this.transactionManager = transactionManager;
    }

    static <I> SQLRepositoryProxyFactory<I> standalone(DataSource dataSource) {
        SqlConnectionProvider connectionProvider = new SqlConnectionProvider();
        return new SQLRepositoryProxyFactory<>(dataSource, connectionProvider, new TransactionManager(connectionProvider));
    }

    @Override
    public I createProxy(Class<I> interf) {
        var handler = new SQLRepositoryProxyMethodHandler(dataSource, connectionProvider, transactionManager, interf);
        Object proxy = Proxy.newProxyInstance(interf.getClassLoader(), new Class[]{interf}, handler);
        return interf.cast(proxy);
    }
}
