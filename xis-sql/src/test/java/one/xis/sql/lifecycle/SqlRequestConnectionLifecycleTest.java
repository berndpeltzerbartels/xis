package one.xis.sql.lifecycle;

import one.xis.context.AppContext;
import one.xis.context.Component;
import one.xis.http.RequestContext;
import one.xis.sql.Entity;
import one.xis.sql.Insert;
import one.xis.sql.Param;
import one.xis.sql.Repository;
import one.xis.sql.Select;
import one.xis.sql.SimpleDataSource;
import one.xis.sql.Transactional;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class SqlRequestConnectionLifecycleTest {

    @Test
    void requestScopedSimpleDataSourceReusesConnectionWithoutTransaction() throws Exception {
        ConnectionEvents events = new ConnectionEvents();
        DataSource dataSource = countingDataSource(simpleDataSource("sql-simple-request-lifecycle"), events);
        AppContext context = context(dataSource);
        events.reset();
        LifecyclePersonReadService service = context.getSingleton(LifecyclePersonReadService.class);

        RequestContext.createInstance(null, null);
        try {
            assertEquals(List.of("Ada", "Ada"), service.readTwice(1L));
            assertEquals(0, events.closeCount.get());
            assertEquals(0, events.commitCount.get());
            assertEquals(0, events.rollbackCount.get());
            assertEquals(0, events.disableAutoCommitCount.get());
            assertEquals(1, events.openCount.get());

            RequestContext.getInstance().closeResources(null);
        } finally {
            RequestContext.clear();
        }

        assertEquals(1, events.closeCount.get());
    }

    @Test
    void requestScopedExternalDataSourceClosesEachNonTransactionalRepositoryConnectionImmediately() throws Exception {
        ConnectionEvents events = new ConnectionEvents();
        DataSource dataSource = countingDataSource(jdbcDataSource("sql-external-request-lifecycle"), events);
        AppContext context = context(dataSource);
        events.reset();
        LifecyclePersonReadService service = context.getSingleton(LifecyclePersonReadService.class);

        RequestContext.createInstance(null, null);
        try {
            assertEquals(List.of("Ada", "Ada"), service.readTwice(1L));
            assertEquals(2, events.openCount.get());
            assertEquals(2, events.closeCount.get());

            RequestContext.getInstance().closeResources(null);
        } finally {
            RequestContext.clear();
        }

        assertEquals(2, events.closeCount.get());
        assertEquals(0, events.commitCount.get());
        assertEquals(0, events.rollbackCount.get());
        assertEquals(0, events.disableAutoCommitCount.get());
    }

    @Test
    void transactionalServiceMethodCommitsAndClosesConnectionAtRequestEnd() throws Exception {
        ConnectionEvents events = new ConnectionEvents();
        DataSource dataSource = countingDataSource(simpleDataSource("sql-transaction-commit-lifecycle"), events);
        AppContext context = context(dataSource);
        events.reset();
        LifecyclePersonWriteService service = context.getSingleton(LifecyclePersonWriteService.class);

        RequestContext.createInstance(null, null);
        try {
            service.insertTwoPeople();
            assertEquals(1, events.disableAutoCommitCount.get());
            assertEquals(1, events.restoreAutoCommitCount.get());
            assertEquals(1, events.commitCount.get());
            assertEquals(0, events.rollbackCount.get());
            assertEquals(0, events.closeCount.get());
            assertEquals(1, events.openCount.get());

            RequestContext.getInstance().closeResources(null);
        } finally {
            RequestContext.clear();
        }

        assertEquals(1, events.closeCount.get());
        assertEquals(List.of("Ada", "Grace", "Katherine"), allNames(dataSource));
    }

    @Test
    void nestedTransactionalServiceCallJoinsExistingTransaction() throws Exception {
        ConnectionEvents events = new ConnectionEvents();
        DataSource dataSource = countingDataSource(simpleDataSource("sql-nested-transaction-lifecycle"), events);
        AppContext context = context(dataSource);
        events.reset();
        LifecycleOuterWriteService service = context.getSingleton(LifecycleOuterWriteService.class);

        RequestContext.createInstance(null, null);
        try {
            service.insertThroughNestedService();
            assertEquals(1, events.disableAutoCommitCount.get());
            assertEquals(1, events.restoreAutoCommitCount.get());
            assertEquals(1, events.commitCount.get());
            assertEquals(0, events.rollbackCount.get());
            assertEquals(1, events.openCount.get());

            RequestContext.getInstance().closeResources(null);
        } finally {
            RequestContext.clear();
        }

        assertEquals(1, events.closeCount.get());
        assertEquals(List.of("Ada", "Grace", "Katherine"), allNames(dataSource));
    }

    @Test
    void classLevelTransactionalServiceMethodCommits() throws Exception {
        ConnectionEvents events = new ConnectionEvents();
        DataSource dataSource = countingDataSource(simpleDataSource("sql-class-transaction-lifecycle"), events);
        AppContext context = context(dataSource);
        events.reset();
        LifecycleClassLevelWriteService service = context.getSingleton(LifecycleClassLevelWriteService.class);

        RequestContext.createInstance(null, null);
        try {
            service.insertTwoPeople();
            assertEquals(1, events.disableAutoCommitCount.get());
            assertEquals(1, events.restoreAutoCommitCount.get());
            assertEquals(1, events.commitCount.get());
            assertEquals(0, events.rollbackCount.get());
            assertEquals(1, events.openCount.get());

            RequestContext.getInstance().closeResources(null);
        } finally {
            RequestContext.clear();
        }

        assertEquals(1, events.closeCount.get());
        assertEquals(List.of("Ada", "Grace", "Katherine"), allNames(dataSource));
    }


    @Test
    void transactionalServiceMethodRollsBackAndClosesConnectionAtRequestEnd() throws Exception {
        ConnectionEvents events = new ConnectionEvents();
        DataSource dataSource = countingDataSource(simpleDataSource("sql-transaction-rollback-lifecycle"), events);
        AppContext context = context(dataSource);
        events.reset();
        LifecyclePersonWriteService service = context.getSingleton(LifecyclePersonWriteService.class);

        RequestContext.createInstance(null, null);
        try {
            assertThrows(RuntimeException.class, service::insertPersonAndFail);
            assertEquals(1, events.disableAutoCommitCount.get());
            assertEquals(1, events.restoreAutoCommitCount.get());
            assertEquals(0, events.commitCount.get());
            assertEquals(1, events.rollbackCount.get());
            assertEquals(0, events.closeCount.get());
            assertEquals(1, events.openCount.get());

            RequestContext.getInstance().closeResources(null);
        } finally {
            RequestContext.clear();
        }

        assertEquals(1, events.closeCount.get());
        assertEquals(List.of("Ada"), allNames(dataSource));
    }

    private AppContext context(DataSource dataSource) {
        return AppContext.builder()
                .withSingleton(dataSource)
                .withPackage("one.xis.sql")
                .withPackage(SqlRequestConnectionLifecycleTest.class.getPackageName())
                .build();
    }

    private SimpleDataSource simpleDataSource(String name) throws SQLException {
        SimpleDataSource dataSource = new SimpleDataSource();
        dataSource.setUrl("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1");
        createSchema(dataSource);
        return dataSource;
    }

    private JdbcDataSource jdbcDataSource(String name) throws SQLException {
        JdbcDataSource dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:" + name + ";DB_CLOSE_DELAY=-1");
        createSchema(dataSource);
        return dataSource;
    }

    private void createSchema(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("drop table if exists people");
            statement.execute("create table people (id bigint primary key, first_name varchar(100))");
            statement.execute("insert into people values (1, 'Ada')");
        }
    }

    private List<String> allNames(DataSource dataSource) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             var resultSet = statement.executeQuery("select first_name from people order by id")) {
            var names = new java.util.ArrayList<String>();
            while (resultSet.next()) {
                names.add(resultSet.getString(1));
            }
            return names;
        }
    }

    private DataSource countingDataSource(DataSource delegate, ConnectionEvents events) {
        return (DataSource) Proxy.newProxyInstance(
                DataSource.class.getClassLoader(),
                new Class[]{DataSource.class},
                (proxy, method, args) -> {
                    try {
                        Object result = method.invoke(delegate, args);
                        if (result instanceof Connection connection) {
                            events.openCount.incrementAndGet();
                            return countingConnection(connection, events);
                        }
                        return result;
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }

    private Connection countingConnection(Connection connection, ConnectionEvents events) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    switch (method.getName()) {
                        case "close" -> events.closeCount.incrementAndGet();
                        case "commit" -> events.commitCount.incrementAndGet();
                        case "rollback" -> events.rollbackCount.incrementAndGet();
                        case "setAutoCommit" -> countAutoCommitChange(args, events);
                        default -> {
                        }
                    }
                    try {
                        return method.invoke(connection, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }

    private void countAutoCommitChange(Object[] args, ConnectionEvents events) {
        if (Boolean.FALSE.equals(args[0])) {
            events.disableAutoCommitCount.incrementAndGet();
        } else if (Boolean.TRUE.equals(args[0])) {
            events.restoreAutoCommitCount.incrementAndGet();
        }
    }

    private static class ConnectionEvents {
        private final AtomicInteger openCount = new AtomicInteger();
        private final AtomicInteger closeCount = new AtomicInteger();
        private final AtomicInteger commitCount = new AtomicInteger();
        private final AtomicInteger rollbackCount = new AtomicInteger();
        private final AtomicInteger disableAutoCommitCount = new AtomicInteger();
        private final AtomicInteger restoreAutoCommitCount = new AtomicInteger();

        private void reset() {
            openCount.set(0);
            closeCount.set(0);
            commitCount.set(0);
            rollbackCount.set(0);
            disableAutoCommitCount.set(0);
            restoreAutoCommitCount.set(0);
        }
    }

}

interface LifecyclePersonReadService {
    List<String> readTwice(long id);
}

@Component
class LifecyclePersonReadServiceImpl implements LifecyclePersonReadService {
    private final LifecyclePersonRepository repository;

    LifecyclePersonReadServiceImpl(LifecyclePersonRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<String> readTwice(long id) {
        return List.of(repository.nameById(id), repository.nameById(id));
    }
}

interface LifecyclePersonWriteService {
    @Transactional
    void insertTwoPeople();

    @Transactional
    void insertPersonAndFail();
}

@Component
class LifecyclePersonWriteServiceImpl implements LifecyclePersonWriteService {
    private final LifecyclePersonRepository repository;

    LifecyclePersonWriteServiceImpl(LifecyclePersonRepository repository) {
        this.repository = repository;
    }

    @Override
    public void insertTwoPeople() {
        repository.insertPerson(2L, "Grace");
        repository.insertPerson(3L, "Katherine");
    }

    @Override
    public void insertPersonAndFail() {
        repository.insertPerson(2L, "Grace");
        repository.insertPerson(1L, "Duplicate Ada");
    }
}

interface LifecycleOuterWriteService {
    @Transactional
    void insertThroughNestedService();
}

@Component
class LifecycleOuterWriteServiceImpl implements LifecycleOuterWriteService {
    private final LifecyclePersonRepository repository;
    private final LifecycleNestedWriteService nestedService;

    LifecycleOuterWriteServiceImpl(LifecyclePersonRepository repository, LifecycleNestedWriteService nestedService) {
        this.repository = repository;
        this.nestedService = nestedService;
    }

    @Override
    public void insertThroughNestedService() {
        repository.insertPerson(2L, "Grace");
        nestedService.insertTwoPeopleTail();
    }
}

interface LifecycleNestedWriteService {
    @Transactional
    void insertTwoPeopleTail();
}

@Component
class LifecycleNestedWriteServiceImpl implements LifecycleNestedWriteService {
    private final LifecyclePersonRepository repository;

    LifecycleNestedWriteServiceImpl(LifecyclePersonRepository repository) {
        this.repository = repository;
    }

    @Override
    public void insertTwoPeopleTail() {
        repository.insertPerson(3L, "Katherine");
    }
}

interface LifecycleClassLevelWriteService {
    void insertTwoPeople();
}

@Transactional
@Component
class LifecycleClassLevelWriteServiceImpl implements LifecycleClassLevelWriteService {
    private final LifecyclePersonRepository repository;

    LifecycleClassLevelWriteServiceImpl(LifecyclePersonRepository repository) {
        this.repository = repository;
    }

    @Override
    public void insertTwoPeople() {
        repository.insertPerson(2L, "Grace");
        repository.insertPerson(3L, "Katherine");
    }
}

@Repository
interface LifecyclePersonRepository {

    @Select("select first_name from people where id = {id}")
    String nameById(@Param("id") long id);

    @Insert("insert into people (id, first_name) values ({id}, {firstName})")
    int insertPerson(@Param("id") long id, @Param("firstName") String firstName);
}

@Entity("people")
class LifecyclePerson {
    long id;
    String firstName;
}
