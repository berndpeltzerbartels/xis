package one.xis.example.login;

import one.xis.remote.ClientState;
import one.xis.remote.Method;
import one.xis.remote.Widget;

@Widget
public class Login {

    @ClientState
    private LoginState loginState;

    @Method
    public boolean login() {
        return true;
    }

}
