package one.xis.remote.javascript;

import java.util.List;

public interface JSBlockStatement extends JSStatement, JSStatementHolder {

    void addStatement(JSStatement statement);

    @Override
    List<JSStatement> getStatements();
}
