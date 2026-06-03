package one.xis.sql;

import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import one.xis.context.AppContext;
import one.xis.context.Component;
import one.xis.context.Inject;
import one.xis.context.Init;
import one.xis.http.RequestContext;

import javax.sql.DataSource;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SQLRepositoryProxyFactoryTest {

    private JdbcDataSource dataSource;
    private PersonRepository repository;

    @BeforeEach
    void setUp() throws SQLException {
        dataSource = new JdbcDataSource();
        dataSource.setURL("jdbc:h2:mem:sql-repository-proxy-factory;DB_CLOSE_DELAY=-1");
        try (var connection = dataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("drop table if exists people");
            statement.execute("create table people (id bigint primary key, first_name varchar(100))");
            statement.execute("insert into people values (1, 'Ada')");
        }
        repository = SQLRepositoryProxyFactory.<PersonRepository>standalone(dataSource).createProxy(PersonRepository.class);
    }

    @Test
    void findsByPrimaryKey() {
        var person = repository.findById(1L);

        assertTrue(person.isPresent());
        assertEquals("Ada", person.get().firstName);
    }

    @Test
    void findsAll() {
        List<Person> people = repository.findAll();

        assertEquals(1, people.size());
        assertEquals("Ada", people.get(0).firstName);
    }

    @Test
    void countsRows() {
        assertEquals(1, repository.count());
    }

    @Test
    void savesEntity() {
        Person person = new Person();
        person.id = 2L;
        person.firstName = "Grace";

        Person returned = repository.save(person);

        assertSame(person, returned);
        assertEquals("Grace", repository.findById(2L).orElseThrow().firstName);
    }

    @Test
    void deletesEntity() {
        Person person = repository.findById(1L).orElseThrow();

        assertTrue(repository.delete(person));

        assertTrue(repository.findById(1L).isEmpty());
    }

    @Test
    void deletesById() {
        assertTrue(repository.deleteById(1L));

        assertEquals(0, repository.count());
    }

    @Test
    void handlesAnnotatedMethodsBesideGenericMethods() {
        assertEquals("Ada", repository.nameById(1L));
    }

    @Test
    void commitsDefaultRepositoryMethodTransaction() {
        repository.insertTwoPeople();

        assertEquals(3, repository.count());
        assertEquals("Grace", repository.nameById(2L));
        assertEquals("Katherine", repository.nameById(3L));
    }

    @Test
    void rollsBackDefaultRepositoryMethodTransactionOnException() {
        assertThrows(RuntimeException.class, repository::insertPersonAndFail);

        assertEquals(1, repository.count());
    }

    @Test
    void rollsBackDefaultRepositoryMethodTransactionDeclaredOnRepositoryType() {
        var repository = SQLRepositoryProxyFactory.<TypeAnnotatedPersonRepository>standalone(dataSource)
                .createProxy(TypeAnnotatedPersonRepository.class);

        assertThrows(RuntimeException.class, repository::insertPersonAndFail);

        assertEquals(1, this.repository.count());
    }

    @Test
    void defaultRepositoryMethodWithoutTransactionKeepsPreviousStatements() {
        assertThrows(RuntimeException.class, repository::insertPersonAndFailWithoutTransaction);

        assertEquals(2, repository.count());
        assertEquals("Grace", repository.nameById(2L));
    }

    @Test
    void requestScopedTransactionCommitsAtRequestEnd() {
        var provider = new SqlConnectionProvider();
        var manager = new TransactionManager(provider);
        var repository = repository(dataSource, provider, manager);
        var transaction = new SqlTransaction(manager);

        RequestContext.createInstance(null, null);
        try {
            transaction.begin();
            repository.insertPerson(2L, "Grace");
            RequestContext.getInstance().closeResources(null);
        } finally {
            RequestContext.clear();
        }

        assertEquals("Grace", this.repository.nameById(2L));
    }

    @Test
    void requestScopedTransactionRollsBackAtRequestEndWhenRequestFails() {
        var provider = new SqlConnectionProvider();
        var manager = new TransactionManager(provider);
        var repository = repository(dataSource, provider, manager);
        var transaction = new SqlTransaction(manager);

        RequestContext.createInstance(null, null);
        try {
            transaction.begin();
            repository.insertPerson(2L, "Grace");
            RequestContext.getInstance().closeResources(new RuntimeException("request failed"));
        } finally {
            RequestContext.clear();
        }

        assertEquals(1, this.repository.count());
    }

    @Test
    void requestScopedNonTransactionalRepositoryCallClosesConnectionImmediately() {
        var closeCount = new AtomicInteger();
        DataSource countingDataSource = countingDataSource(closeCount);
        var provider = new SqlConnectionProvider();
        var manager = new TransactionManager(provider);
        var repository = repository(countingDataSource, provider, manager);

        RequestContext.createInstance(null, null);
        try {
            assertEquals("Ada", repository.nameById(1L));
            assertEquals(1, closeCount.get());
            RequestContext.getInstance().closeResources(null);
        } finally {
            RequestContext.clear();
        }

        assertEquals(1, closeCount.get());
    }

    @Test
    void requestScopedSimpleDataSourceWithoutPoolingReusesConnectionUntilRequestEnd() throws Exception {
        var closeCount = new AtomicInteger();
        SimpleDataSource simpleDataSource = new SimpleDataSource();
        simpleDataSource.setUrl("jdbc:h2:mem:sql-simple-request-reuse;DB_CLOSE_DELAY=-1");
        try (var connection = simpleDataSource.getConnection();
             var statement = connection.createStatement()) {
            statement.execute("create table people (id bigint primary key, first_name varchar(100))");
            statement.execute("insert into people values (1, 'Ada')");
        }
        DataSource countingDataSource = countingDataSource(simpleDataSource, closeCount);
        var provider = new SqlConnectionProvider();
        var manager = new TransactionManager(provider);
        var repository = repository(countingDataSource, provider, manager);

        RequestContext.createInstance(null, null);
        try {
            assertEquals("Ada", repository.nameById(1L));
            assertEquals("Ada", repository.nameById(1L));
            assertEquals(0, closeCount.get());
            RequestContext.getInstance().closeResources(null);
        } finally {
            RequestContext.clear();
        }

        assertEquals(1, closeCount.get());
    }

    @Test
    void failedRepositoryCallMarksRequestTransactionForRollback() {
        var provider = new SqlConnectionProvider();
        var manager = new TransactionManager(provider);
        var repository = repository(dataSource, provider, manager);
        var transaction = new SqlTransaction(manager);

        RequestContext.createInstance(null, null);
        try {
            transaction.begin();
            repository.insertPerson(2L, "Grace");
            assertThrows(RuntimeException.class, () -> repository.insertPerson(1L, "Duplicate Ada"));
            RequestContext.getInstance().closeResources(null);
        } finally {
            RequestContext.clear();
        }

        assertEquals(1, this.repository.count());
    }

    @Test
    void createsAndInjectsRepositoryProxyThroughContext() {
        var context = AppContext.builder()
                .withSingleton(dataSource)
                .withPackage(SQLRepositoryProxyFactoryTest.class.getPackageName())
                .build();

        var consumer = context.getSingleton(RepositoryConsumer.class);

        assertEquals("Ada", consumer.nameById(1L));
    }

    @Test
    void rollsBackTransactionalServiceMethodDeclaredOnInterface() {
        var context = AppContext.builder()
                .withSingleton(dataSource)
                .withPackage(SQLRepositoryProxyFactoryTest.class.getPackageName())
                .build();
        var service = context.getSingleton(PersonWriteService.class);

        assertThrows(RuntimeException.class, service::createPersonAndFail);

        assertEquals(1, repository.count());
    }

    @Test
    void rollsBackTransactionalServiceMethodDeclaredOnInterfaceType() {
        var context = AppContext.builder()
                .withSingleton(dataSource)
                .withPackage(SQLRepositoryProxyFactoryTest.class.getPackageName())
                .build();
        var service = context.getSingleton(TypeAnnotatedPersonWriteService.class);

        assertThrows(RuntimeException.class, service::createPersonAndFailOnInterfaceType);

        assertEquals(1, repository.count());
    }

    @Test
    void rollsBackTransactionalServiceMethodDeclaredOnImplementation() {
        var context = AppContext.builder()
                .withSingleton(dataSource)
                .withPackage(SQLRepositoryProxyFactoryTest.class.getPackageName())
                .build();
        var service = context.getSingleton(ImplementationAnnotatedPersonWriteService.class);

        assertThrows(RuntimeException.class, service::createPersonAndFailOnImplementation);

        assertEquals(1, repository.count());
    }

    @Test
    void commitsTransactionalServiceMethod() {
        var context = AppContext.builder()
                .withSingleton(dataSource)
                .withPackage(SQLRepositoryProxyFactoryTest.class.getPackageName())
                .build();
        var service = context.getSingleton(PersonWriteService.class);

        service.createPerson(2L, "Grace");

        assertEquals("Grace", repository.nameById(2L));
    }

    @Test
    void allowsSchemaCreationInInitMethodBeforeRepositoryIsUsed() {
        var lateDataSource = new JdbcDataSource();
        lateDataSource.setURL("jdbc:h2:mem:sql-late-schema;DB_CLOSE_DELAY=-1");

        var context = AppContext.builder()
                .withSingleton(lateDataSource)
                .withPackage(SQLRepositoryProxyFactoryTest.class.getPackageName())
                .build();

        var consumer = context.getSingleton(LateRepositoryConsumer.class);

        assertEquals("Late Ada", consumer.nameById(1L));
    }

    @Test
    void createsSimpleDataSourceWhenNoDataSourceExists() throws SQLException {
        createDefaultDataSourceTable();

        var context = AppContext.builder()
                .withPackage(SQLRepositoryProxyFactoryTest.class.getPackageName())
                .build();

        var repository = context.getSingleton(PersonRepository.class);

        assertEquals("Default Ada", repository.nameById(1L));
    }

    @Test
    void validatesMissingUrlOnlyForSimpleDataSource() {
        var dataSource = new SimpleDataSource();
        var validator = new DataSourceValidator(new DataSourceProvider(dataSource));

        var exception = assertThrows(IllegalStateException.class, validator::validate);

        assertEquals("Property 'xis.sql.url' is required when the default SQL DataSource is used", exception.getMessage());
    }

    @Test
    void ignoresExternalDataSource() {
        var validator = new DataSourceValidator(new DataSourceProvider(dataSource));

        assertDoesNotThrow(validator::validate);
    }

    private void createDefaultDataSourceTable() throws SQLException {
        try (var connection = DriverManager.getConnection("jdbc:h2:mem:sql-default-datasource;DB_CLOSE_DELAY=-1");
             var statement = connection.createStatement()) {
            statement.execute("drop table if exists people");
            statement.execute("create table people (id bigint primary key, first_name varchar(100))");
            statement.execute("insert into people values (1, 'Default Ada')");
        }
    }

    private DataSource countingDataSource(AtomicInteger closeCount) {
        return countingDataSource(dataSource, closeCount);
    }

    private DataSource countingDataSource(DataSource delegate, AtomicInteger closeCount) {
        return (DataSource) Proxy.newProxyInstance(
                DataSource.class.getClassLoader(),
                new Class[]{DataSource.class},
                (proxy, method, args) -> {
                    try {
                        Object result = method.invoke(delegate, args);
                        if (result instanceof Connection connection) {
                            return countingConnection(connection, closeCount);
                        }
                        return result;
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }

    private PersonRepository repository(DataSource dataSource, SqlConnectionProvider provider, TransactionManager manager) {
        return new SQLRepositoryProxyFactory<PersonRepository>(new DataSourceProvider(dataSource), provider, manager)
                .createProxy(PersonRepository.class);
    }

    private static Connection countingConnection(Connection connection, AtomicInteger closeCount) {
        return (Connection) Proxy.newProxyInstance(
                Connection.class.getClassLoader(),
                new Class[]{Connection.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("close")) {
                        closeCount.incrementAndGet();
                    }
                    try {
                        return method.invoke(connection, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
    }

    @Component
    static class RepositoryConsumer {
        @Inject
        PersonRepository repository;

        String nameById(long id) {
            return repository.nameById(id);
        }
    }

    @Component
    static class LateSchemaInitializer {
        private final DataSourceProvider dataSourceProvider;

        LateSchemaInitializer(DataSourceProvider dataSourceProvider) {
            this.dataSourceProvider = dataSourceProvider;
        }

        @Init
        void initialize() throws SQLException {
            try (var connection = dataSourceProvider.dataSource().getConnection();
                 var statement = connection.createStatement()) {
                statement.execute("drop table if exists late_people");
                statement.execute("create table late_people (id bigint primary key, first_name varchar(100))");
                statement.execute("insert into late_people values (1, 'Late Ada')");
            }
        }
    }

    @Component
    static class LateRepositoryConsumer {
        @Inject
        LatePersonRepository repository;

        String nameById(long id) {
            return repository.findById(id).orElseThrow().firstName;
        }
    }

    interface PersonWriteService {
        @Transactional
        void createPersonAndFail();

        @Transactional
        void createPerson(long id, String firstName);
    }

    @Component
    static class PersonWriteServiceImpl implements PersonWriteService {
        private final PersonRepository repository;

        PersonWriteServiceImpl(PersonRepository repository) {
            this.repository = repository;
        }

        @Override
        public void createPersonAndFail() {
            repository.insertPerson(2L, "Grace");
            repository.insertPerson(1L, "Duplicate Ada");
        }

        @Override
        public void createPerson(long id, String firstName) {
            repository.insertPerson(id, firstName);
        }
    }

    @Transactional
    interface TypeAnnotatedPersonWriteService {
        void createPersonAndFailOnInterfaceType();
    }

    @Component
    static class TypeAnnotatedPersonWriteServiceImpl implements TypeAnnotatedPersonWriteService {
        private final PersonRepository repository;

        TypeAnnotatedPersonWriteServiceImpl(PersonRepository repository) {
            this.repository = repository;
        }

        @Override
        public void createPersonAndFailOnInterfaceType() {
            repository.insertPerson(2L, "Grace");
            repository.insertPerson(1L, "Duplicate Ada");
        }
    }

    interface ImplementationAnnotatedPersonWriteService {
        void createPersonAndFailOnImplementation();
    }

    @Component
    static class ImplementationAnnotatedPersonWriteServiceImpl implements ImplementationAnnotatedPersonWriteService {
        private final PersonRepository repository;

        ImplementationAnnotatedPersonWriteServiceImpl(PersonRepository repository) {
            this.repository = repository;
        }

        @Override
        @Transactional
        public void createPersonAndFailOnImplementation() {
            repository.insertPerson(2L, "Grace");
            repository.insertPerson(1L, "Duplicate Ada");
        }
    }

    @Repository
    interface PersonRepository extends CrudRepository<Person, Long> {

        @Select("select first_name from people where id = {id}")
        String nameById(@Param("id") long id);

        @Insert("insert into people (id, first_name) values ({id}, {firstName})")
        int insertPerson(@Param("id") long id, @Param("firstName") String firstName);

        @Transactional
        default void insertTwoPeople() {
            insertPerson(2L, "Grace");
            insertPerson(3L, "Katherine");
        }

        @Transactional
        default void insertPersonAndFail() {
            insertPerson(2L, "Grace");
            insertPerson(1L, "Duplicate Ada");
        }

        default void insertPersonAndFailWithoutTransaction() {
            insertPerson(2L, "Grace");
            insertPerson(1L, "Duplicate Ada");
        }
    }

    @Transactional
    @Repository
    interface TypeAnnotatedPersonRepository extends CrudRepository<Person, Long> {

        @Insert("insert into people (id, first_name) values ({id}, {firstName})")
        int insertPerson(@Param("id") long id, @Param("firstName") String firstName);

        default void insertPersonAndFail() {
            insertPerson(2L, "Grace");
            insertPerson(1L, "Duplicate Ada");
        }
    }

    @Entity("people")
    static class Person {
        long id;
        String firstName;
    }

    @Repository
    interface LatePersonRepository extends CrudRepository<LatePerson, Long> {
    }

    @Entity("late_people")
    static class LatePerson {
        long id;
        String firstName;
    }
}
