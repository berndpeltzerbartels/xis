package one.xis.remote.js2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@RequiredArgsConstructor
class JSObjectDeclaration implements JSStatementPart {
    private final String name;
    private final List<JSObjectMember> members = new ArrayList<>();

    JSFieldDeclaration addField(String name) {
        JSFieldDeclaration fieldDeclaration = new JSFieldDeclaration(name);
        members.add(fieldDeclaration);
        return fieldDeclaration;
    }

    JSMethodDeclaration addMethod(String name, String... parameternames) {
        JSMethodDeclaration methodDeclaration = new JSMethodDeclaration(name);
        methodDeclaration.addParams(parameternames);
        members.add(methodDeclaration);
        return methodDeclaration;
    }

    @Override
    public String getRef() {
        return name;
    }
}
