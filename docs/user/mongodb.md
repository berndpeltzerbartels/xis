# MongoDB

[Documentation map](../README.md)

`xis-mongodb` provides a small MongoDB integration for XIS Boot applications and for applications that want to use MongoDB
without Spring Data.

The module is intentionally modest. It focuses on three things:

- CRUD repositories for document classes
- explicit JSON queries with `@MongoQuery`
- MongoDB change streams through `@MongoWatch`

## Dependency

`build.gradle`

```groovy
plugins {
    id "java"
    id "one.xis.plugin" version "0.13.1"
}

repositories {
    mavenCentral()
}

dependencies {
    implementation "one.xis:xis-boot" // or xis-spring
    implementation "one.xis:xis-mongodb"
}
```

When the XIS Gradle plugin is used, omit the XIS version as usual. The MongoDB Java driver is brought by `xis-mongodb`;
you do not need to add it separately for normal use.

## Configuration

`xis-mongodb` creates a default `MongoClient` and `MongoDatabase` from properties:

```properties
xis.mongo.connection-string=mongodb://localhost:27017
xis.mongo.database=crm
```

The connection string is optional and defaults to the MongoDB driver's normal local connection. The database name is
required when the default `MongoDatabase` bean is used.

## Documents

Annotate document classes or records with `@MongoDocument`:

```java
@MongoDocument("customers")
record Customer(String id, String firstName, String lastName) {
}
```

The field or record component named `id` maps to MongoDB `_id`. Use `@MongoId` when the id has another Java name.

```java
@MongoDocument("customers")
class Customer {
    @MongoId
    String customerId;

    @MongoField("first_name")
    String firstName;

    String lastName;

    @MongoIgnore
    String internalNote;
}
```

## Repositories

A repository is an interface annotated with `@MongoRepository` and extending `MongoCrudRepository`.

```java
@MongoRepository
interface CustomerRepository extends MongoCrudRepository<Customer, String> {

    @MongoQuery("{ lastName: ?0 }")
    List<Customer> findByLastName(String lastName);
}
```

`MongoCrudRepository` provides:

- `findById`
- `findAll`
- `save`
- `delete`
- `deleteById`
- `count`

`@MongoQuery` takes MongoDB JSON. Positional placeholders such as `?0` are replaced with method arguments. This is
deliberately explicit: XIS does not try to invent a query language or derive MongoDB queries from method names.

## Change Streams

MongoDB change streams can be connected to XIS components with `@MongoWatch`:

```java
@Component
class CustomerChanges {
    private final RefreshEventPublisher refreshEvents;

    CustomerChanges(RefreshEventPublisher refreshEvents) {
        this.refreshEvents = refreshEvents;
    }

    @MongoWatch(Customer.class)
    void customerChanged(MongoChangeEvent<Customer> event) {
        refreshEvents.publish(new RefreshEvent("customers"));
    }
}
```

The method can receive either `MongoChangeEvent<T>` or the changed document type directly:

```java
@MongoWatch(Customer.class)
void customerChanged(Customer customer) {
}
```

`MongoChangeEvent` contains the document type, collection name, MongoDB operation name, and the mapped document. The
watcher uses MongoDB's `UPDATE_LOOKUP` mode so update events include the current full document when the server supports
that.

Change streams require MongoDB support for change streams, typically a replica set or compatible managed deployment.
