package one.xis.js;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class JSScript implements JSContext {
    List<JSClass> classDeclarations = new ArrayList<>();
    List<JSStatement> statements = new ArrayList<>();
    List<JSVarAssignment> globalVars = new ArrayList<>();

    public void addClassDeclaration(JSClass declaration) {
        classDeclarations.add(declaration);
    }

    public void addStatement(JSStatement statement) {
        statements.add(statement);
    }

    public JSVar addSingleton(JSVar jsVar, JSClass jsClass) {
        globalVars.add(new JSVarAssignment(jsVar, new JSContructorCall(jsClass)));
        return jsVar;
    }

    public JSVar addSingleton(String name, JSClass jsClass) {
        return addSingleton(new JSVar(name), jsClass);
    }


}
