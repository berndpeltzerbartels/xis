package one.xis.js;

import lombok.Value;

import java.util.ArrayList;
import java.util.List;

@Value
public class JSScript implements JSContext {
    List<JSDeclaration> declarations = new ArrayList<>();
    List<JSFunction> funtionDeclarations = new ArrayList<>();
    List<JSStatement> statements = new ArrayList<>();

    public void addDeclaration(JSDeclaration declaration) {
        declarations.add(declaration);
    }

    public void addFunction(JSFunction jsFunction) {
        funtionDeclarations.add(jsFunction);
    }

}
