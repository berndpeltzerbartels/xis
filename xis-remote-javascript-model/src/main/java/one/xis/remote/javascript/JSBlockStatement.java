package one.xis.remote.javascript;

import java.util.List;

public interface JSBlockStatement extends JSStatement {

    void addStatement(JSStatement statement);

    List<JSStatement> getStatements();
}
