package one.xis.remote.example.login;

import one.xis.remote.Container;
import one.xis.remote.Method;

@Container
public class Login {

    @Method
    public boolean login(String user, String password) {
        return true;
    }

}
