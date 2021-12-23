package one.xis.remote.example.login;

import one.xis.remote.Method;
import one.xis.remote.PageComponent;

@PageComponent
public class Login {

    @Method
    public boolean login(String user, String password) {
        return true;
    }

}
