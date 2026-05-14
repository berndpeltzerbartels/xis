package one.xis.sql;

import one.xis.context.ProxyMethodHandler;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

class SQLRepositoryProxyMethodHandler extends ProxyMethodHandler {
    private final Map<Method, SQLMethodHandler> methodHandlers = new HashMap<>();
    private final TransactionManager transactionManager;
    private final boolean repositoryTransactional;

    SQLRepositoryProxyMethodHandler(DataSource dataSource, SqlConnectionProvider connectionProvider,
                                    TransactionManager transactionManager, Class<?> repositoryInterface) {
        this.transactionManager = transactionManager;
        this.repositoryTransactional = repositoryInterface.isAnnotationPresent(Transactional.class);
        DataSource transactionAwareDataSource = new TransactionAwareDataSource(dataSource, connectionProvider);
        RepositoryMetadata metadata = null;
        for (Method method : repositoryInterface.getMethods()) {
            if (needsCrudMetadata(method) && metadata == null) {
                metadata = new RepositoryMetadata(repositoryInterface);
            }
            SQLMethodHandler handler = methodHandler(transactionAwareDataSource, metadata, method);
            if (handler != null) {
                methodHandlers.put(method, new LazySQLMethodHandler(method, handler));
            }
        }
    }

    @Override
    protected Object doInvoke(Object proxy, Method method, Object[] args) {
        try {
            if (repositoryTransactional || method.isAnnotationPresent(Transactional.class)) {
                return transactionManager.invoke(() -> invokeRepositoryMethod(proxy, method, args));
            }
            return invokeRepositoryMethod(proxy, method, args);
        } catch (RuntimeException | Error e) {
            transactionManager.markRollbackOnly(e);
            throw e;
        }
    }

    private Object invokeRepositoryMethod(Object proxy, Method method, Object[] args) {
        if (method.isDefault()) {
            return invokeDefaultMethod(proxy, method, args);
        }
        SQLMethodHandler handler = methodHandlers.get(method);
        if (handler == null) {
            throw new UnsupportedOperationException("No SQL repository handler for method " + method);
        }
        return handler.invoke(args);
    }

    private Object invokeDefaultMethod(Object proxy, Method method, Object[] args) {
        try {
            return InvocationHandler.invokeDefault(proxy, method, args == null ? new Object[0] : args);
        } catch (RuntimeException | Error e) {
            throw e;
        } catch (Throwable e) {
            throw new RuntimeException("Error invoking default repository method " + method, e);
        }
    }

    private SQLMethodHandler methodHandler(DataSource dataSource, RepositoryMetadata metadata, Method method) {
        if (method.isAnnotationPresent(Select.class)) {
            return new SelectMethodHandler(dataSource, new ROMapper());
        }
        if (method.isAnnotationPresent(Insert.class) || method.isAnnotationPresent(Update.class)) {
            return new UpdateMethodHandler(dataSource);
        }
        if (method.isAnnotationPresent(Save.class)) {
            return new SaveMethodHandler(dataSource);
        }
        if (method.isAnnotationPresent(Delete.class)) {
            return new DeleteMethodHandler(dataSource);
        }
        if (method.isAnnotationPresent(Function.class) || method.isAnnotationPresent(StoredProcedure.class)) {
            return new CallableMethodHandler(dataSource);
        }
        return genericMethodHandler(dataSource, metadata, method);
    }

    private SQLMethodHandler genericMethodHandler(DataSource dataSource, RepositoryMetadata metadata, Method method) {
        if (!method.getDeclaringClass().equals(CrudRepository.class)) {
            return null;
        }
        if (metadata == null) {
            throw new IllegalArgumentException("Generic repository method needs CrudRepository metadata: " + method);
        }
        return switch (method.getName()) {
            case "findById" -> new GenericFindByIdMethodHandler(dataSource, metadata.entityType());
            case "findAll" -> new GenericFindAllMethodHandler(dataSource, metadata.entityType());
            case "save" -> new SaveMethodHandler(dataSource, metadata.entityType());
            case "delete" -> new DeleteMethodHandler(dataSource, metadata.entityType());
            case "deleteById" -> new GenericDeleteByIdMethodHandler(dataSource, metadata.entityType());
            case "count" -> new GenericCountMethodHandler(dataSource, metadata.entityType());
            default -> null;
        };
    }

    private boolean needsCrudMetadata(Method method) {
        return method.getDeclaringClass().equals(CrudRepository.class);
    }

    private static class LazySQLMethodHandler implements SQLMethodHandler {
        private final Method method;
        private final SQLMethodHandler delegate;
        private boolean initialized;

        private LazySQLMethodHandler(Method method, SQLMethodHandler delegate) {
            this.method = method;
            this.delegate = delegate;
        }

        @Override
        public boolean matches(Method method) {
            return delegate.matches(method);
        }

        @Override
        public void init(Method method) {
            delegate.init(method);
            initialized = true;
        }

        @Override
        public Object invoke(Object[] args) {
            initializeIfNeeded();
            return delegate.invoke(args);
        }

        private void initializeIfNeeded() {
            if (initialized) {
                return;
            }
            synchronized (this) {
                if (!initialized) {
                    init(method);
                }
            }
        }
    }
}
