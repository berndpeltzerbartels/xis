package one.xis.example;

import one.xis.remote.Variable;
import one.xis.remote.Widget;

import java.util.List;

@Widget
public interface MainPage {

    void setMainContent(@Variable("main") Object content, Object... args);

    void setMessage(@Variable("message") String message);

    void setErrorMessages(@Variable("errorMessage") List<String> errorMessages);
}
