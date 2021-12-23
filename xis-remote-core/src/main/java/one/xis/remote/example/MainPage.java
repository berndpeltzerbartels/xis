package one.xis.remote.example;

import one.xis.remote.Binding;
import one.xis.remote.PageComponent;

import java.util.List;

@PageComponent
public interface MainPage {

    void setMainContent(@Binding("main") Object content, Object... args);

    void setMessage(@Binding("message") String message);

    void setErrorMessages(@Binding("errorMessage") List<String> errorMessages);
}
