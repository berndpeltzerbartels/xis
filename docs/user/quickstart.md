# Quickstart

This quickstart creates a small XIS application with Spring Boot. The standalone XIS Boot runtime follows the same page,
template, and action model; only the application entry point and dependencies differ.

## Prerequisites

- Java 17 or newer
- Gradle 8 or newer

## Gradle Setup

`settings.gradle`

```groovy
rootProject.name = "my-xis-app"
```

`build.gradle`

```groovy
plugins {
    id "java"
    id "org.springframework.boot" version "3.3.0"
    id "io.spring.dependency-management" version "1.1.5"
    id "one.xis.plugin" version "<xis-version>"
}

group = "example"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation "one.xis:xis-spring"

    testImplementation "one.xis:xis-test"
    testImplementation "org.springframework.boot:spring-boot-starter-test"
}

tasks.withType(JavaCompile) {
    options.encoding = "UTF-8"
}

test {
    useJUnitPlatform()
}
```

## Application Class

`src/main/java/example/Application.java`

```java
package example;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class Application {

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }
}
```

## First Page

`src/main/java/example/dashboard/DashboardPage.java`

```java
package example.dashboard;

import one.xis.ModelData;
import one.xis.Page;
import one.xis.WelcomePage;

@WelcomePage
@Page("/index.html")
public class DashboardPage {

    @ModelData
    public String title() {
        return "Dashboard";
    }
}
```

`src/main/java/example/dashboard/DashboardPage.html`

```html
<!DOCTYPE html>
<html xmlns:xis="https://xis.one/xsd" lang="en">
<head>
    <meta charset="UTF-8">
    <title>${title}</title>
</head>
<body>
    <h1>${title}</h1>
    <p>Your first XIS page is running.</p>
</body>
</html>
```

Run the app:

```bash
./gradlew bootRun
```

Open:

```text
http://localhost:8080/
```

`@WelcomePage` marks the default entry page. The page is also available directly at `/index.html`.

## Add an Action

`src/main/java/example/dashboard/CounterPage.java`

```java
package example.dashboard;

import one.xis.Action;
import one.xis.ModelData;
import one.xis.Page;

@Page("/counter.html")
public class CounterPage {

    private int count;

    @ModelData
    public int count() {
        return count;
    }

    @Action
    public void increment() {
        count++;
    }
}
```

`src/main/java/example/dashboard/CounterPage.html`

```html
<!DOCTYPE html>
<html xmlns:xis="https://xis.one/xsd" lang="en">
<head>
    <meta charset="UTF-8">
    <title>Counter</title>
</head>
<body>
    <h1>Counter</h1>
    <p>Current count: ${count}</p>
    <button xis:action="increment">Increment</button>
</body>
</html>
```

The action is invoked through XIS. You do not create a REST endpoint for it.

## Generate Templates

The Gradle plugin can generate missing template files for page and frontlet controllers:

```bash
./gradlew templates
```

Generated templates are a starting point. Keep the Java controller and the HTML template in the same package unless you
explicitly configure another template location.
