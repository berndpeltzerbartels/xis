package one.xis.remote.js2;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RequiredArgsConstructor
class JSMethodDeclaration implements JSObjectMember {
    private final String name;
    private List<String> parameterNames = new ArrayList<>();
    private final List<JSStatement> statements = new ArrayList<>();

    JSMethodDeclaration(String name, List<String> parameterNames) {
        this.name = name;
        this.parameterNames.addAll(parameterNames);
    }

    JSMethodDeclaration addStatement(JSStatement statement) {
        statements.add(statement);
        return this;
    }

    JSMethodDeclaration addParam(String name) {
        parameterNames.add(name);
        return this;
    }

    JSMethodDeclaration addParams(String... names) {
        parameterNames.addAll(Arrays.asList(names));
        return this;
    }


    public JSMethodDeclaration addStatements(List<JSStatement> statementList) {
        statements.addAll(statementList);
        return this;
    }
}
