package one.xis.remote.js2;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
class JSFieldDeclaration implements JSObjectMember, JSStatementPart {
    private final String name;

    @Override
    public String getRef() {
        return "this." + name;
    }
}
