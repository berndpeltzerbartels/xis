# XIS JavaScript Framework - AI Assistant Guidelines

## Architecture Overview

This is a client-side web application framework with server integration built around a **tag handler pattern** and **widget-based architecture**. The framework uses dependency injection and event-driven communication.

**Important**: This code is bundled from an upper project - there are no ES6 imports/exports. All dependencies are resolved through global variables and constructor injection. Some duplication exists due to this bundling approach.

### Core Components

- **Application** (`app/classes/Application.js`): Main dependency container that wires up all services and components
- **PageController** (`classes/page/PageController.js`): Manages page lifecycle, URL routing, and server communication
- **TagHandlers** (`classes/handler/`): DOM manipulation layer - each HTML element type has a corresponding handler class
- **Widgets** (`classes/widget/`): Reusable UI components that load data from server endpoints
- **BackendService** (`classes/BackendService.js`): Handles server communication and data loading for widgets

## Key Patterns

### Tag Handler Pattern
All DOM manipulation goes through handler classes that extend `TagHandler`. Each handler:
- Manages a specific DOM element and its lifecycle
- Uses expression parsing for data binding (`${expression}` syntax)
- Forms parent-child relationships with other handlers
- Example: `FormHandler` manages form submission, `InputTagHandler` manages input fields

### Widget System
Widgets are server-backed components:
- Load HTML templates and data from backend
- Use `WidgetInstance` and `WidgetState` for state management
- Communicate through `WidgetContainers` for nested widget hierarchies

### Data Binding & Expression Language
- Uses custom expression parser (`classes/parse/ExpressionParser.js`) for `${...}` expressions
- Hierarchical data model (`classes/Data.js`) supports nested scopes
- EL functions available for expressions (`classes/parse/ELFunctions.js`)

## Development Workflows

### Testing
- Test classes in `test/` directory mirror production structure
- `TestApplication.js` provides mock environment with `HttpConnectorMock`
- Use `app.openPage(uri)` in tests to simulate page navigation

### Debugging
- VS Code debug configuration available (`.vscode/launch.json`)
- Global `app` object available in browser console for runtime inspection
- Event system uses `EventPublisher` - listen to `EventType` constants for debugging

### Adding New Functionality

**New Tag Handler:**
1. Extend `TagHandler` class
2. Implement `refresh(data)` method for data binding
3. Register in `TagHandlers` via `mapHandler(tagName, handlerClass)`
4. Follow naming: `[ElementName]TagHandler` or `[ElementName]Handler`

**New Widget:**
1. Server must provide widget configuration and HTML template
2. Client loads widgets through `Widgets.loadWidgets(config)`
3. Widget data loaded via `BackendService.loadWidgetData()`

## File Organization

- `classes/`: Core framework classes organized by responsibility
- `classes/handler/`: All DOM element handlers
- `classes/connect/`: HTTP client and server communication
- `classes/page/`: Page lifecycle and routing
- `classes/widget/`: Widget system components
- `app/`: Application-specific overrides and customizations
- `functions/`: Global utility functions
- `event-registry/`: Event type definitions and global event registry

## Important Conventions

- Constructor dependency injection pattern throughout
- Promise-based async operations (`.then()` chains, not async/await)
- Global functions defined in `functions/Functions.js`
- Event-driven architecture using `EventPublisher` and `EventType` constants
- Expression language uses `${...}` syntax for data binding
- All handlers maintain parent-child relationships for proper cleanup and data flow

## Common Integration Points

- **Server Communication**: All goes through `HttpClient` implementations
- **DOM Access**: Use `DomAccessor` for consistent DOM manipulation
- **State Management**: `ClientState` and `LocalStore` for client-side persistence
- **URL Handling**: `URLResolver` and `ResolvedURL` for routing and parameter extraction