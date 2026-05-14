# Custom Proxies

[Documentation map](../../README.md) | [Advanced topics](README.md)

Custom proxies are an advanced extension point for libraries that want to expose interface-based components without
making application code construct them by hand. XIS SQL repositories use this mechanism: an interface is annotated, XIS
finds the annotation, asks the configured factory for a proxy, and then injects the proxy like any other component.

Most applications should use existing proxy-based modules such as `xis-sql`. Create your own proxy annotation when you
are building reusable infrastructure: repositories for another datastore, generated clients, typed access to an external
system, or a company library that should feel like a normal injected service.

## Shape Of A Proxy Extension

A proxy extension has three parts:

- an interface that application code injects
- an annotation for those interfaces
- a `ProxyFactory` that creates the runtime implementation

```java
package com.example.audit;

import one.xis.context.Proxy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Proxy(factory = AuditClientProxyFactory.class)
public @interface AuditClient {
}
```

```java
package com.example.audit;

@AuditClient
public interface CustomerAudit {

    void changed(long customerId, String message);
}
```

```java
package com.example.audit;

import one.xis.context.ProxyFactory;

public class AuditClientProxyFactory implements ProxyFactory<CustomerAudit> {

    private final AuditTransport transport;

    public AuditClientProxyFactory(AuditTransport transport) {
        this.transport = transport;
    }

    @Override
    public CustomerAudit createProxy(Class<CustomerAudit> interf) {
        return (customerId, message) -> transport.send(interf.getName(), customerId, message);
    }
}
```

The factory itself can use constructor injection. In XIS Boot it is created by the XIS context. In Spring Boot, XIS
registers interfaces annotated with proxy annotations as Spring beans and lets Spring create the factory, so Spring beans
such as `DataSource`, HTTP clients, or project services can be injected into the factory.

## Using The Proxy

Once the interface is annotated, application code uses it like any other dependency:

```java
import one.xis.Service;

@Service
public class CustomerService {

    private final CustomerAudit audit;

    public CustomerService(CustomerAudit audit) {
        this.audit = audit;
    }

    public void renameCustomer(long customerId, String name) {
        audit.changed(customerId, "renamed to " + name);
    }
}
```

The application does not call the factory directly and does not need a manual `@Bean` method for every interface.

## Interface Advice Proxies

Ordinary context components can also be wrapped by interface advice. That is a separate advanced topic because it is
about cross-cutting behavior around normal service methods, not about generated interface implementations. See
[Aspects and interface advice](aspects.md).

## Factory Name Variant

If the factory type should not be loaded directly from the annotation, use `factoryName`:

```java
@Proxy(factoryName = "com.example.audit.AuditClientProxyFactory")
public @interface AuditClient {
}
```

This is useful for optional integrations where the annotation module should not directly depend on the implementation
module.

## Spring Boot Notes

XIS Spring scans the Spring Boot application packages for interfaces annotated with annotations that themselves carry
`@Proxy`. For every matching interface it registers a Spring bean backed by the proxy factory.

This means:

- constructor injection into Spring components works for custom proxies
- proxy factories can depend on Spring beans
- libraries can provide proxy annotations without requiring handwritten Spring configuration in every application

If a factory depends on optional infrastructure, make sure that infrastructure is available in the Spring context. For
example, SQL repository factories need a `DataSource`; `xis-spring` supplies the default XIS SQL datasource when
`xis-sql` is on the classpath and the application has not defined its own `DataSource`.

## When Not To Use This

Do not use custom proxies to hide ordinary business services. If a class can simply be a `@Service` or `@Component`, keep
it that way. Proxies are best for interface families where the implementation is generated, reflected, remote, or backed
by metadata on the interface.
