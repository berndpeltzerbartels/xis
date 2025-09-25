# XIS Framework - AI Coding Agent Instructions

XIS is a Java web framework for server-side rendered HTML with plain HTML templates and explicit Java controllers. Avoid template engines - XIS promotes clear separation between Java logic and HTML structure.

## Core Architecture

### Controllers & Annotations
- **`@Page("/path.html")`** - Marks page controllers with URL mapping
- **`@Widget`** - Marks reusable pagelet controllers (ID defaults to class SimpleName)
- Controllers map 1:1 with HTML files in same package structure

### Controller Methods
- **`@ModelData`** - Provides data to templates (key = method name or annotation value)
- **`@FormData("key")`** - Handles form binding and validation
- **`@Action("name")`** - Handles user interactions (buttons, links)
- **`@RequestScope`**, **`@ClientState`**, **`@LocalStorage`** - Different data scopes

### HTML Template Integration
- Use `xmlns:xis="https://xis.one/xsd"` namespace
- **`<xis:widget-container container-id="id" default-widget="WidgetClass"/>`** - Embeds pagelets
- **`xis:action="methodName"`** with **`<xis:parameter name="key" value="${value}"/>`**
- **`xis:foreach="item:${items}"`** or **`<xis:foreach array="${items}" var="item">`**
- **`xis:widget="WidgetClass"`** loads pagelets, **`xis:page="/url"`** navigates

## Key Patterns

### Request Flow
1. `ClientRequest` → `ControllerService` → `ControllerWrapper`
2. Controller methods return `PageResponse`, `WidgetResponse`, `Class<?>`, or `void`
3. `ControllerMethodResultMapper` handles navigation logic
4. Widgets/Pages resolved via `PageControllerWrappers`/`WidgetControllerWrappers`

### Multi-Framework Support
- **Spring**: Use `@Component` + XIS annotations
- **Micronaut**: Controllers implement framework-specific interfaces
- Framework adapters (`MicronautController`, Spring equivalents) bridge to `FrontendService`

### JavaScript Integration
- Auto-generated JS in `xis-javascript` module compiles to single file
- `WidgetContainerHandler`, `PageLinkHandler` handle client-side interactions
- No manual JavaScript required for standard interactions

## Development Workflow

### Build & Test
- **Gradle multi-project**: Root + ~30 modules (core: `xis-server`, `xis-context`, `xis-controller-api`)
- **Integration tests**: Use `IntegrationTestContext.builder().withSingleton(PageClass.class).build()`
- **Selenide tests**: Full browser testing in `xis-selenide-tests`

### File Organization
```
MyPage.java          // @Page("/mypage.html") 
MyPage.html          // In same package, uses ${modelData}
MyWidget.java        // @Widget, embedded in pages
MyWidget.html        // Widget template
```

### Common Response Patterns
```java
@Action 
void simpleAction() { } // Stay on current page

@Action
Class<?> navigate() { return OtherPage.class; } // Page navigation

@Action  
WidgetResponse updateWidget() { return new WidgetResponse(SomeWidget.class); }
```

### Testing Controllers
Use `IntegrationTestContext` - no web server needed:
```java 
var context = IntegrationTestContext.builder()
    .withSingleton(MyPage.class).build();
var result = context.openPage(MyPage.class);
assertThat(result.getDocument().getElementById("test").getInnerText()).isEqualTo("expected");
```

## Critical Implementation Details
- `ControllerWrapper` handles method invocation lifecycle
- `PathResolver` manages URL routing and path variables  
- `HtmlResourcePathResolver` locates HTML files by convention
- Framework-agnostic core (`xis-server`) with adapter pattern for Spring/Micronaut
- Widget containers use `container-id` for dynamic content replacement
- All framework configuration via Java annotations - no XML/YAML config files