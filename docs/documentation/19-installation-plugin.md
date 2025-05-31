## 19. Installation and Plugin Usage

To use XIS effectively in your project, a Gradle plugin is available that simplifies setup and dependency management.
This chapter outlines how to install and configure XIS, with and without the plugin.

---

### With the XIS Gradle Plugin (Recommended)

Add the following to your `build.gradle`:

```groovy
plugins {
    id 'one.xis.spring-plugin' version '1.0.0'
}
```

This plugin will:

* Automatically set up required dependencies (`xis-controller-api`, `xis-spring`, `xis-test`)
* Ensure the Java source folder is treated as a resources folder as well
* Prepare default configuration for template, JS, and CSS file discovery

You do **not** need to declare XIS dependencies manually when using the plugin.

---

### Without the Plugin

If you choose not to use the plugin, you must:

1. Declare all required dependencies manually:

```groovy
implementation project(':xis-controller-api')
implementation project(':xis-spring')
implementation project(':xis-test')
```

2. Ensure that the Java source folder is also treated as a resource folder.

In Gradle:

```groovy
sourceSets {
    main {
        resources.srcDirs += 'src/main/java'
    }
}
```

---

### Maven Compatibility (Planned)

A Maven plugin is not yet available. All testing and configuration has been done with **Gradle only**.

> TODO: Build and publish a Maven plugin with equivalent functionality.

---

### Summary

| Setup Style       | Plugin Needed | Manual Dependencies | Source Folder as Resources  | Recommended |
|-------------------|---------------|---------------------|-----------------------------|-------------|
| Gradle + Plugin   | Yes           | No                  | Handled by plugin           | ✅ Yes       |
| Gradle w/o Plugin | No            | Yes                 | Must be configured manually | ⚠️ Advanced |
| Maven             | Planned       | Yes                 | Must be configured manually | ❌ Not yet   |

This setup ensures that XIS remains modular and tightly integrated, following the philosophy of colocating controller,
template, and resource files.
