package one.xis;

public class UserContextAccess {

    public static void removeInstance() {
        UserContextImpl.removeInstance();
    }

    public static void setInstance(UserContextImpl context) {
        UserContextImpl.setInstance(context);
    }
}
