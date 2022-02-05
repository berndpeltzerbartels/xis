package one.xis.remote.js2;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Getter
@RequiredArgsConstructor
class JSFunctionDeclaration {
    private final String name;
    private List<String> parameterNames = new ArrayList<>();
    private final List<JSStatement> statements = new ArrayList<>();

    JSFunctionDeclaration(String name, List<String> parameterNames) {
        this.name = name;
        this.parameterNames.addAll(parameterNames);
    }

    JSFunctionDeclaration addStatement(JSStatement statement) {
        statements.add(statement);
        return this;
    }

    JSFunctionDeclaration addParam(String name) {
        parameterNames.add(name);
        return this;
    }

    JSFunctionDeclaration addParams(String... names) {
        parameterNames.addAll(Arrays.asList(names));
        return this;
    }


}
