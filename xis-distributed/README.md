# XIS Distributed

`xis-distributed` adds distributed routing support to XIS applications.

It is intended for applications where pages and frontlets may be served by different servers, while the browser still
behaves like one coherent XIS application.

## What The Module Does

The module contributes:

- a `XisDistributedConfig` contract for the remote host list
- a default `application.properties` based implementation for XIS Boot
- a `/xis/distributed/hosts` endpoint that exposes the host list to the browser
- a distributed CORS policy based on that host list

The browser first loads the local `/xis/config`, then asks `/xis/distributed/hosts` for remote hosts. For every listed
remote host it loads the normal `/xis/config` from that host and merges the returned pages and frontlets into the local
client config. Requests for those merged pages and frontlets are then sent to the host they came from.

There are no server-side page or frontlet host mappings in this module. The remote applications describe their own
pages and frontlets through their normal XIS client config.

## Main API

The central contract is `XisDistributedConfig`:

```java
interface XisDistributedConfig {
    List<String> getHosts();
}
```

Applications can provide their own implementation when they want full control over host discovery.

## Default Implementation

The module also contains `PropertiesXisDistributedConfig`.

This is a `@DefaultComponent`, which means:

- it acts as the fallback implementation shipped by the module
- a normal application `@Component` can replace it
- a Spring bean implementing `XisDistributedConfig` can replace it as well

So the architectural contract is the interface, not the properties-based implementation.

## Properties Format

If you use the built-in properties-based implementation, it reads a comma-separated list of remote hosts from:

```properties
xis.distributed.hosts=https://shop.example.com,https://catalog.example.com
```

Hosts must include protocol and host, for example `http://localhost:9000` or `https://shop.example.com`.

The list is interpreted from the point of view of the current runtime:

- in the shell runtime it is the list of remote runtimes whose `/xis/config` should be loaded
- in a remote runtime it is also used as the list of allowed distributed browser origins for CORS

That means a remote runtime normally lists the shell host that may call it.

When `xis-distributed` is on the classpath, this property is required unless the application provides its own
`XisDistributedConfig` implementation.

## Relationship To Other Modules

- `xis-server` provides the no-op same-origin behavior used when `xis-distributed` is not present
- `xis-javascript` performs the actual request routing after merging remote client configs
- `xis-context` provides `@DefaultComponent`

## Testing Status

The module is covered by unit tests for:

- host-list properties
- distributed CORS origins
- the host-list endpoint

Browser-level end-to-end coverage can live in `xis-end-to-end-tests` with separate shell and remote runtimes.
