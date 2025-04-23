# Quickstart Guide (Spring Boot)

This guide helps you build your first XIS application using **Spring Boot** and the `xis-spring` integration module.

You'll create:

- A Spring Boot app
- An HTML template
- A Java controller bound to that template using XIS

---

## 1. Create a Spring Boot Project

Use [https://start.spring.io](https://start.spring.io) or set up manually with:

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.2.0'
    id 'io.spring.dependency-management' version '1.1.0'
    id 'one.xis.xis-plugin' version '0.1.0'
}

dependencies {
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'one.xis:xis-spring:0.1.0'
}
```

Run the build once to trigger XIS code generation:

```bash
gradle build
```

Your project structure will look like this:

```
my-xis-app/
├── build.gradle
├── src/
│   └── main/
│       └── java/
│           └── com/example/demo/
│               ├── AppLauncher.java
│               └── greeting/
│                   ├── HelloPage.java
│                   └── HelloPage.html
```

---

## 2. Create the HTML Fragment

In `src/main/java/com/example/demo/greeting/HelloPage.html`:

```html
<h1>${greeting}</h1>
<button onclick="sayHello">Change Greeting</button>
```

XIS will inject the `${greeting}` from your controller and bind the button click to a method.

---

## 3. Create the Java Controller

In `src/main/java/com/example/demo/greeting/HelloPage.java`:

```java
package com.example.demo.greeting;

@WelcomePage
@Page("/greeting.html")
public class HelloPage {

    @ModelData("greeting")
    public String sayHello() {
        return "Hello from Spring Boot + XIS!";
    }
}
```

> 💡 Make sure your controller class is located within a package that Spring scans automatically.
> This typically means: the same or a subpackage of your `@SpringBootApplication` class.
> XIS controllers are Spring components, but no @Component annotation is needed. @Page and @Widget are sufficient
> component annotations.
> (XIS `@Widget`s, however, **are** annotated with `@Component`.)

---

## 4. Run the App

In `src/main/java/com/example/demo/AppLauncher.java`:

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AppLauncher {
    public static void main(String[] args) {
        SpringApplication.run(AppLauncher.class, args);
    }
}
```

Start the app:

```bash
gradle bootRun
```

Open [http://localhost:8080](http://localhost:8080) — you should see the greeting page!

---

## ✅ What’s next?

- Add multiple `@ModelData` values
- Navigate between fragments
- Use `@Push` for real-time updates
- Split your UI into reusable HTML components

For deeper insights, check out:

- [Architecture](architecture.md)
- [Documentation](https://xis.one/docs/)
- [Javadoc](https://javadoc.io/doc/one.xis/xis-spring)

---

> 💡 Note: If you'd like to use XIS without the plugin, see the upcoming [manual setup guide](manual-setup.md) *(TODO)*

