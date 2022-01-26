package one.xis.example.login;

import one.xis.remote.Method;
import one.xis.remote.Widget;

@Widget
public class Login {
    
    private LoginState loginState;

    @Method
    public boolean login() {
        return true;
    }

}
