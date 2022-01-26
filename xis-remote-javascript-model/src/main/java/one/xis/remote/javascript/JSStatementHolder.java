package one.xis.remote.javascript;

import java.util.List;

public interface JSStatementHolder {
    List<JSStatement> getStatements();

    void addStatement(JSStatement statement);
}
