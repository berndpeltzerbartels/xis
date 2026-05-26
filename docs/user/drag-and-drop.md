# Drag And Drop

[Documentation map](../README.md)

Use `xis:drag` and `xis:drop` when a user should drag one value onto a target and the drop should call a normal
server-side `@Action` method.

This is intentionally a small API. It is useful for common cases such as moving a card to another column, assigning an
item to a group, or moving a game piece from one field to another. If you need browser-specific drag previews, multiple
payload values, file drops, or custom pointer behavior, use custom JavaScript.

Drag and drop actions are testable with normal XIS integration tests. You do not need a real browser just to verify that
a drop calls the expected `@Action` method with the expected values; use `DragAndDrop` from `xis-test` for that.

## Basic Pattern

The drag element defines one value and gives that value a name:

```html
<span xis:drag="from:${field}">Piece</span>
```

The drop element calls an action. The named drag value is available as an action argument:

```html
<div xis:drop="move(from, target='${targetField}')">Target</div>
```

The action method receives the evaluated arguments:

```java
@Action
void move(@ActionParameter("from") String from, @ActionParameter("target") String target) {
    chessService.move(from, target);
}
```

In this example, `from` is the value from the dragged element and `target` is the value from the drop target.
Name drop arguments with `name=expression` when the value is not already a simple variable. Other injected parameters,
such as `@SharedValue` or `@ClientId`, do not affect action-parameter binding.

## Full Example

```html
<div xis:foreach="item:${items}">
    <span xis:drag="itemId:${item.id}">
        ${item.label}
    </span>
</div>

<section xis:foreach="list:${lists}">
    <h2>${list.name}</h2>
    <div xis:drop="move(itemId, listId='${list.id}')">
        Drop here
    </div>
</section>
```

```java
@Page("/board.html")
class BoardPage {

    private final BoardService boardService;

    BoardPage(BoardService boardService) {
        this.boardService = boardService;
    }

    @Action
    void move(@ActionParameter("itemId") String itemId, @ActionParameter("listId") String listId) {
        boardService.move(itemId, listId);
    }
}
```

`xis:drag` uses `name:expression` syntax. The name is the temporary variable that is available during the drop action.
The expression is evaluated in the model context of the dragged element.

`xis:drop` uses action-call syntax. A simple argument such as `itemId` is also sent as a named action parameter with
that name. Use `name=expression` for constants or values from the drop target's model context.

## Keep The Payload Simple

`xis:drag` carries one value. That keeps the feature predictable and keeps the HTML readable.

When you need more than one value, build a compact string yourself and decode it in the action:

```html
<span xis:drag="move:${piece.id + ':' + square.id}">${piece.label}</span>
<div xis:drop="movePiece(move, targetSquare='${targetSquare.id}')"></div>
```

For richer browser behavior, prefer handwritten JavaScript. XIS drag and drop does not prevent you from implementing a
custom solution for special cases.

## Testing

Integration tests can execute the same drag and drop flow with `DragAndDrop` from `xis-test`:

```java
import one.xis.test.dom.DragAndDrop;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Test
void movesItemToTarget() {
    var result = context.openPage("/board.html");
    var document = result.getDocument();

    new DragAndDrop(
            document.getElementById("item-42"),
            document.getElementById("archive")
    ).doDragAndDrop();

    assertEquals("42", boardService.lastMovedItemId());
    assertEquals("archive", boardService.lastTargetId());
}
```

See [Examples and tests](examples-and-tests.md#test-drag-and-drop) for the integration-test API.
