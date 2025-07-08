package one.xis.server;

import java.util.function.Consumer;

public interface LocalUrlHolder extends UrlHolder {

    void setLocalUrl(String localUrl);

    boolean localUrlIsSet();

    void addUrlAssignmentListener(Consumer<String> listener);
}
