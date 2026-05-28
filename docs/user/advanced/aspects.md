# Aspects And Interface Advice

[Documentation map](../../../README.md) | [Advanced topics](README.md)

XIS Boot has a small AOP-style mechanism for cross-cutting behavior. It is deliberately narrower than AspectJ or Spring
AOP: advice is applied only to components that are used through an interface. XIS does not modify classes and does not
use bytecode generation.

This limitation is intentional. Interface proxies keep the runtime friendly to native-image builds, avoid bytecode and
classloader surprises, produce clearer error messages, and keep object lifetimes simple for the garbage collector. The
price is explicit architecture: code that should be advised must be used through an interface.

Use this when a library or application needs behavior such as timing, transactions, locking, retries, or auditing around
service method calls.

## Shape

Create an annotation and mark that annotation with `@UseAdvice`:

```java
import one.xis.context.UseAdvice;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.ElementType.TYPE;

@UseAdvice(TimedAdvice.class)
@Target({TYPE, METHOD})
@Retention(RetentionPolicy.RUNTIME)
@interface Timed {
}
```

The advice receives an `AdviceInvocation`. It can inspect the method, arguments, annotations, target object, and context,
and then call `proceed()`.

```java
import one.xis.context.Advice;
import one.xis.context.AdviceInvocation;

class TimedAdvice implements Advice {

    @Override
    public Object around(AdviceInvocation invocation) throws Throwable {
        long start = System.nanoTime();
        try {
            return invocation.proceed();
        } finally {
            long millis = (System.nanoTime() - start) / 1_000_000;
            System.out.println(invocation.method().getName() + " took " + millis + " ms");
        }
    }
}
```

## Usage

Apply the annotation to an implementation method and inject the service through its interface:

```java
interface CustomerService {
    void update(Customer customer);
}
```

```java
import one.xis.context.Service;

@Service
class CustomerServiceImpl implements CustomerService {

    @Timed
    public void update(Customer customer) {
        // business logic
    }
}
```

```java
@Service
class CustomerPageService {

    private final CustomerService customerService;

    CustomerPageService(CustomerService customerService) {
        this.customerService = customerService;
    }
}
```

The annotation may also be placed on the implementation class or the service interface. Type-level advice applies to all
methods that are called through the proxied interface.

The original object remains the target for dependency injection, `@Init`, `@Bean`, `@Scheduled`, and other framework
reflection. The object injected into interface consumers is a JDK proxy.

## Limits

- A component must be used through an interface to be advised.
- XIS validates advice placement at context startup. Advice on a class without a usable interface, or on a method that
  is not declared by such an interface, fails fast.
- Injecting the concrete implementation class bypasses advice.
- Self-invocation is not intercepted. A call like `this.update(...)` inside `CustomerServiceImpl` is a normal Java call.
- Advice classes currently need a no-argument constructor.

For generated interface proxies such as repository proxies, see [Custom proxies](custom-proxies.md).
