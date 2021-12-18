package one.xis.remote.example.login;

import one.xis.remote.RemoteMethod;
import one.xis.remote.RemoteService;

@RemoteService
public class LoginRemoteService {

    @RemoteMethod
    public boolean login(String user, String password) {
        return true;
    }

}
