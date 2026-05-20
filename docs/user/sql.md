# SQL

[Documentation map](../README.md)

`xis-sql` provides a small repository layer for applications that want direct SQL instead of a full ORM.

The module is intentionally close to the database model: entities are mapped from rows, repositories are interfaces, and
SQL annotations are used when the generated CRUD methods are not enough.

XIS SQL is not a persistence context like JPA. Reading a row creates an object, but changing that object never changes
the database by itself, not even inside a transaction. Database writes happen only when repository methods such as
`@Insert`, `@Update`, `@Save`, or `@Delete` are invoked. This keeps write behavior explicit and avoids hidden flushes.

## Dependency

`build.gradle`

```groovy
plugins {
    id "java"
    id "one.xis.plugin" version "0.12.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation "one.xis:xis-boot" // or xis-spring
    implementation "one.xis:xis-sql"
    runtimeOnly "com.h2database:h2:2.2.224" // or another JDBC driver
}
```

When the XIS Gradle plugin is used, omit the XIS version as usual.

## DataSource

In XIS Boot applications, `xis-sql` creates a simple default `DataSource` when no other `DataSource` exists:

```properties
xis.sql.url=jdbc:h2:file:./data/app
xis.sql.user=sa
xis.sql.password=
xis.sql.driver-class-name=org.h2.Driver
```

Only `xis.sql.url` is required for the default `DataSource`. The other properties are optional. The default
implementation is deliberately small and does not pool connections unless pooling is explicitly enabled. Production
applications can provide their own `DataSource`; it automatically replaces the default one.

### Optional Connection Pool

XIS can create a HikariCP-backed pool when HikariCP is on the application classpath and pooling is enabled:

`build.gradle`

```groovy
plugins {
    id "java"
    id "one.xis.plugin" version "0.12.0"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation "one.xis:xis-boot" // or xis-spring
    implementation "one.xis:xis-sql"
    runtimeOnly "org.postgresql:postgresql:42.7.11" // or another JDBC driver
    runtimeOnly "com.zaxxer:HikariCP:5.1.0"
}
```

```properties
xis.sql.url=jdbc:postgresql://localhost:5432/app
xis.sql.user=app
xis.sql.password=secret
xis.sql.pool.enabled=true
```

If `xis.sql.pool.enabled=true` is set and HikariCP is missing, startup fails with a clear error message. Missing pool
values use defaults:

```properties
xis.sql.pool.maximum-pool-size=10
xis.sql.pool.minimum-idle=2
xis.sql.pool.connection-timeout=30000
xis.sql.pool.idle-timeout=600000
xis.sql.pool.max-lifetime=1800000
```

Invalid pool values fail fast. `minimum-idle` must not be greater than `maximum-pool-size`, and all numeric pool values
must be greater than zero.

In Spring applications, a Spring-managed `DataSource` is imported into the XIS context. XIS does not create a second
`DataSource` in that case.

## Entity Mapping

Annotate entities with `@Entity`. Field names map directly to column names, and camel-case field names also map to
underscore column names.

```java
@Entity("customers")
record Customer(long id, String firstName, String lastName) {
}
```

Use `@Column` when the database column name does not follow those conventions. Use `@NoColumn` or `transient` for fields
that are not backed by a column. `@NoColumn` is explicit SQL documentation: the property is never read from or written
to SQL, even if a matching column exists. `@Ignore` is still supported as an older equivalent.

XIS distinguishes three mapped property kinds:

- SQL-simple values such as strings, numbers, booleans, enums, UUIDs, and date/time values are mapped as normal JDBC
  values.
- `@Entity` fields and `List<EntityType>` fields are mapped as SQL relations through database foreign-key metadata.
- Properties annotated with `@JsonColumn` are mapped to one SQL column containing JSON. Use this for small value objects
  or collections of values that belong to the owning row and should not become their own table.

XIS does not need an `@Id` annotation. It reads primary keys from database metadata. Generated `CrudRepository`
methods are intentionally limited to tables with exactly one primary-key column. For composite primary keys, define the
needed repository methods explicitly with `@Select`, `@Insert`, `@Update`, `@Save`, or `@Delete`.

`@OptionalColumn` marks a property as mapped only when the column exists. It is mainly intended for reusable entities:
for example a library class may expose roles as a JSON column while one application persists them in the same table and
another loads them from a separate role table in its `UserInfoService`.

```java
@Entity("employees")
class Employee extends UserInfoImpl {
    long id;
    String password;
}
```

`UserInfoImpl` already marks its OpenID Connect profile fields as optional SQL columns. Its `roles` property is both
`@OptionalColumn` and `@JsonColumn`: if `employees.roles` exists, XIS reads and writes it as JSON. If the column is
missing, generated `@Insert`, `@Update`, and `@Save` statements omit it and result mapping leaves the property unchanged.
`userId` is not optional because it is the stable local user id used for authentication tokens. This is intentionally
explicit: `@OptionalColumn` is for cross-project entity reuse, not for hiding ordinary spelling mistakes in column names.

## Example With Relations

This example maps one customer with many orders. The Java model follows the database model: the `orders.customer_id`
foreign key points to `customers.id`.

Keep relation graphs small and intentional. XIS does not support lazy loading, and it is usually a better design to load
only the aggregate you actually need. Relations such as invoice and invoice item are natural candidates for one loaded
object graph. Broad models such as `Customer` with every `Order`, every contact, and every secondary object hanging off
it are usually harder to reason about and easier to break. Prefer separate repositories and explicit queries when objects
are not tightly coupled.

```sql
create table customers (
    id bigint primary key,
    first_name varchar(100),
    last_name varchar(100)
);

create table orders (
    id bigint primary key,
    customer_id bigint not null,
    total decimal(12, 2),
    constraint fk_orders_customer foreign key (customer_id) references customers(id)
);
```

```java
import one.xis.sql.Entity;

import java.math.BigDecimal;
import java.util.List;

@Entity("customers")
class Customer {
    long id;
    String firstName;
    String lastName;
    List<Order> orders;
}

@Entity("orders")
class Order {
    long id;
    BigDecimal total;
}
```

The repository can combine generated CRUD methods with explicit SQL:

```java
import one.xis.sql.CrudRepository;
import one.xis.sql.Delete;
import one.xis.sql.Param;
import one.xis.sql.Repository;
import one.xis.sql.Save;
import one.xis.sql.Select;
import one.xis.sql.Update;

import java.util.List;
import java.util.Optional;

@Repository
interface CustomerRepository extends CrudRepository<Customer, Long> {

    @Select("""
            select c.id, c.first_name, c.last_name,
                   o.id as orders_id, o.total as orders_total
            from customers c
            left join orders o on o.customer_id = c.id
            where c.id = {id}
            """)
    Optional<Customer> findWithOrders(@Param("id") long id);

    @Select("select * from customers where last_name = {lastName}")
    List<Customer> findByLastName(@Param("lastName") String lastName);

    @Save
    Customer saveCustomer(Customer customer);

    @Update("update customers set last_name = {lastName} where id = {id}")
    boolean rename(@Param("id") long id, @Param("lastName") String lastName);

    @Delete
    boolean deleteCustomer(Customer customer);

    @Delete("delete from customers where id = {id}")
    int deleteOnlyCustomerRow(@Param("id") long id);
}
```

`findWithOrders` returns one `Customer` whose `orders` list is assembled from the joined rows. `@Save` stores the
customer row and then inserts or updates the order rows, setting `orders.customer_id` from the customer primary key.
`@Delete` without SQL deletes the object graph from the leaves toward the root so foreign-key constraints are respected.
The explicit `@Delete("...")` method is just a normal SQL statement and does not perform entity cascade handling.

## Repositories

A repository is an interface annotated with `@Repository`. Generic CRUD repositories are available for entities with a
single-column primary key.

```java
@Repository
interface CustomerRepository extends CrudRepository<Customer, Long> {

    @Select("select * from customers where last_name = {lastName}")
    List<Customer> findByLastName(@Param("lastName") String lastName);
}
```

`CrudRepository` provides:

- `findById`
- `findAll`
- `save`
- `delete`
- `deleteById`
- `count`

Custom methods can use `@Select`, `@Insert`, `@Update`, `@Save`, `@Delete`, `@Function`, `@StoredProcedure`, and
`@Transactional`. Named SQL parameters are bound from method parameters annotated with `@Param`. If a method has one
unannotated entity parameter, named placeholders can also refer directly to its properties, for example `{firstName}`.
For annotated entity parameters, use dotted property names such as `{customer.firstName}`.

## Select

`@Select` maps query results to one of three return shapes:

- a single value or entity
- `Optional<T>`
- `List<T>` or another collection type

Simple return types are read from the first result column. Entity return types are mapped by column names. Joined rows
can populate nested entity fields and collection fields. For collections, repeated parent columns are collapsed into one
parent object and the child values are collected.

Relationships are resolved through database foreign-key metadata and `@Entity` table names. A collection field such as
`List<Order>` requires the element type to be an entity and the child table to have a foreign key to the parent table.
One-to-one entity fields are supported when the current table has a foreign key to the referenced table. Bidirectional
relations are rejected.

## Insert And Update

`@Insert` and `@Update` can either execute explicit SQL or derive SQL from one entity parameter.

With explicit SQL, parameters can be bound with JDBC `?` placeholders or with named placeholders:

```java
@Insert("insert into customers (id, first_name, last_name) values ({id}, {firstName}, {lastName})")
int insert(@Param("id") long id,
           @Param("firstName") String firstName,
           @Param("lastName") String lastName);
```

Entity parameters can be used directly:

```java
@Insert("insert into customers (id, first_name, last_name) values ({id}, {firstName}, {lastName})")
Customer insert(Customer customer);
```

If the annotation value is empty, the method must have exactly one entity parameter. XIS reads the table metadata and
builds the statement:

```java
@Insert
int insert(Customer customer);

@Update
boolean update(Customer customer);
```

For `@Update`, all non-primary-key columns are written and all primary-key columns are used in the `where` clause. This
works for single-column and composite primary keys.

Valid return values for `@Insert`, `@Update`, `@Save`, and `@Delete` are:

- `void`
- `boolean`, true when at least one row was changed
- an integer number containing the JDBC update count: `byte`, `short`, `int`, `long`, their wrapper types, or
  `BigInteger`
- one of the method parameter types, usually to return the changed entity

When a method returns one of its parameter types, that returned parameter does not need to be referenced by the SQL
statement. If no parameter matches the return type, startup fails with an exception. If the returned parameter is an
entity parameter and the SQL uses its properties, those properties are still bound normally.

## Save

`@Save` has two modes.

Without SQL, `@Save` is metadata-driven. It has exactly one entity parameter. XIS reads the table primary key from
database metadata, checks whether a row with that key already exists, and then inserts or updates. Single-column and
composite primary keys are supported for the saved root entity.

Single entity fields are stored through foreign-key columns on the saved table. Collection fields are saved as child
rows: the parent row is saved first, then each collection element is inserted or updated and receives the parent's
primary key in its foreign-key column.

`@Save` does not delete collection elements that are no longer present in memory. Use `@Delete` or explicit SQL for
delete semantics.

With SQL, `@Save` behaves like a normal modification statement:

```java
@Save("insert into customers (id, first_name, last_name) values ({id}, {firstName}, {lastName})")
Customer insert(Customer customer);
```

This form does not inspect foreign-key metadata and does not cascade through object relations.

## Delete

`@Delete` has two modes.

Without SQL, `@Delete` deletes one entity instance:

```java
@Delete
boolean deleteCustomer(Customer customer);
```

XIS reads primary-key and foreign-key metadata from the database. It deletes child collections from the leaves toward the
root and then deletes the root row. If a foreign key declares `ON DELETE CASCADE`, XIS skips the redundant direct delete
for that child table, but still evaluates deeper child relations first because those deeper constraints may not cascade.
For composite primary keys, `@Delete` deletes the root entity by all primary-key columns. Object graph cascade handling
is available for single-column primary-key relations.

With SQL, `@Delete` behaves like `@Insert` or `@Update`:

```java
@Delete("delete from customers where id = {id}")
int deleteOnlyCustomerRow(@Param("id") long id);
```

This form does not inspect entity metadata and does not cascade through object relations.

## Transactions

XIS Boot uses interface-based advice for cross-cutting behavior. It does not modify concrete classes and does not use
bytecode generation. This keeps the runtime simple, fast, easy to debug, and friendly to native-image builds.

**`@Transactional`, like other XIS AOP annotations, only works when the component has an application interface and is
called through that interface. XIS intentionally avoids bytecode enhancement.**

`@Transactional` is `one.xis.sql.Transactional`. It is advice, not a marker that repository handlers inspect directly.
That is important: XIS can only apply it when it creates an interface proxy. Calling a concrete implementation directly is
just a normal Java method call and will not open a transaction.

For application code, the most common shape is the Spring-like one: put `@Transactional` on the implementing service
method, but inject and call the service through its interface:

```java
import one.xis.sql.Transactional;

@Service
class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customers;
    private final AuditLogRepository auditLog;

    CustomerServiceImpl(CustomerRepository customers, AuditLogRepository auditLog) {
        this.customers = customers;
        this.auditLog = auditLog;
    }

    @Override
    @Transactional
    public void createCustomer(Customer customer) {
        customers.save(customer);
        auditLog.insert("created customer " + customer.id());
    }
}
```

You may also put the annotation on the interface method:

```java
interface CustomerService {

    @Transactional
    void createCustomer(Customer customer);
}
```

The same type-level variant also works on the service interface itself.

If every method of a service should run in a transaction, annotate the implementing class:

```java
@Transactional
@Service
class CustomerServiceImpl implements CustomerService {

    public void createCustomer(Customer customer) {
        customers.save(customer);
        auditLog.insert("created customer " + customer.id());
    }
}
```

Type-level `@Transactional` applies to all methods called through the proxied interface. In all cases, inject and call the
service through the interface. Injecting `CustomerServiceImpl` instead of `CustomerService` bypasses interface advice.

The JDBC connection is opened lazily. Entering a transactional method only marks the current request/thread as
transactional. The connection is opened when the first repository method actually needs it.

If the method completes normally, XIS commits the transaction. If it throws, XIS rolls it back. Nested transactional
calls join the already active transaction. XIS intentionally keeps the rule simple: there are no JPA-style rollback
rules for checked versus unchecked exceptions.

`@Transactional` is still available for repository default methods. It is useful when the transaction boundary is truly a
repository operation:

```java
@Repository
interface CustomerRepository {

    @Insert("insert into customers (id, first_name, last_name) values ({id}, {firstName}, {lastName})")
    int insertCustomer(@Param("id") long id,
                       @Param("firstName") String firstName,
                       @Param("lastName") String lastName);

    @Insert("insert into audit_log (message) values ({message})")
    int insertAuditLog(@Param("message") String message);

    @Transactional
    default void createCustomer(long id, String firstName, String lastName) {
        insertCustomer(id, firstName, lastName);
        insertAuditLog("created customer " + id);
    }
}
```

During an HTTP request, an open transaction is completed automatically when request processing ends:

- normal request completion commits the transaction
- request failure rolls the transaction back
- a failing repository call marks the transaction for rollback even if the exception is caught later

## Functions And Stored Procedures

`@Function` calls a database function. The Java method return value is the function return value:

```java
@Function("calculate_discount")
BigDecimal calculateDiscount(@Param("customerId") long customerId);
```

XIS calls it through JDBC as a callable statement with a function return slot:

```text
{ ? = call calculate_discount(?) }
```

`@StoredProcedure` calls a database procedure. Without an OUT parameter, it behaves like a modifying statement and can
return `void`, `boolean`, a number, or one of the method parameter types:

```java
@StoredProcedure("archive_customer")
int archiveCustomer(@Param("customerId") long customerId);
```

A stored procedure may also expose exactly one OUT parameter as the Java return value. The OUT parameter is declared on
the annotation and is appended after all Java method parameters:

```java
@StoredProcedure(value = "calculate_discount", out = "discount")
BigDecimal calculateDiscount(@Param("customerId") long customerId);
```

This maps to a callable statement shaped like:

```text
call calculate_discount(?, ?)
```

The first placeholder is the `customerId` IN parameter. The second placeholder is the registered OUT parameter. The
`out` name documents the database procedure parameter and must not duplicate a method `@Param` name.
