package one.xis.js;

import lombok.Value;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Value
public class JSScript implements JSContext {
    List<JSDeclaration> declarations = new ArrayList<>();
    List<JSStatement> statements = new ArrayList<>();

    public void addDeclaration(JSDeclaration declaration) {
        declarations.add(declaration);
    }

    public <D extends JSDeclaration> void addDeclarations(Collection<D> decls) {
        declarations.addAll(decls);
    }

    public void addStatement(JSStatement statement) {
        statements.add(statement);
    }

}
