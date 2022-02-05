package one.xis.remote.js2;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class JSReturn implements JSStatement {
    private final String value;

    JSReturn(JSStatementPart part) {
        value = part.getRef();
    }
}
