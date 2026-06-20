# Explicit SQL Transactions

[Documentation map](../../../README.md) | [Advanced topics](README.md)

Most application code should use `@Transactional` on a service interface method. That keeps the transaction boundary
close to the business operation and lets XIS Boot open, commit, and roll back the transaction through interface advice.

Explicit transactions are available for special cases: infrastructure code, code that does not naturally sit behind an
interface, or code that must end a transaction before request processing ends.

In ordinary application code, prefer `@Transactional`. It is the better default because the transaction boundary is
declared on the business method and XIS opens, commits, or rolls back the transaction automatically through interface
advice. Manual `SqlTransaction` use should be the exception, not the normal style.

```java
@Page("/customers.html")
class CustomerPage {
    private final CustomerRepository customers;
    private final AuditLogRepository auditLog;
    private final SqlTransaction transaction;

    CustomerPage(CustomerRepository customers, AuditLogRepository auditLog, SqlTransaction transaction) {
        this.customers = customers;
        this.auditLog = auditLog;
        this.transaction = transaction;
    }

    @Action("create")
    void createCustomer(@FormData("customer") Customer customer) {
        // Prefer @Transactional on a service method when the work fits that model.
        // Manual begin() is for special cases that need an explicit request-scoped transaction.
        transaction.begin();
        customers.save(customer);
        auditLog.insert("created customer " + customer.id());
        // In an HTTP request this open transaction is committed when request processing ends.
    }
}
```

The JDBC connection is opened lazily. Calling `transaction.begin()` only marks the current request/thread as
transactional. The connection is opened when the first repository method actually needs it.

During an HTTP request, an open explicit transaction is completed automatically when request processing ends:

- normal request completion commits the transaction
- request failure rolls the transaction back
- a failing repository call marks the transaction for rollback even if the exception is caught later

Call `transaction.commit()` or `transaction.rollback()` only when the transaction must end before the request ends, or
when using XIS SQL outside an HTTP request.

```java
transaction.begin();
try {
    customers.save(customer);
    auditLog.insert("created customer " + customer.id());
    transaction.commit(); // Explicit early end of the transaction.
} catch (RuntimeException e) {
    transaction.rollback();
    throw e;
}
```
