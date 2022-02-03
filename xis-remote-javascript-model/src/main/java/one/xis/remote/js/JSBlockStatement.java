package one.xis.remote.js;

import java.util.List;

public interface JSBlockStatement extends JSStatement {
    List<JSStatement> getStatements();
}
