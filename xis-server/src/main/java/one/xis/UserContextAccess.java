package one.xis;

public class UserContextAccess {

    public static void removeInstance() {
        UserContext.removeInstance();
    }

    public static void setInstance(UserContext context) {
        UserContext.setInstance(context);
    }
}
