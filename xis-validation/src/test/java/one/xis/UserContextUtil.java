package one.xis;

import lombok.experimental.UtilityClass;

@UtilityClass
public class UserContextUtil {

    public static UserContext getUserContext() {
        return UserContext.getInstance();
    }

    public static void setUserContext(UserContext userContext) {
        UserContext.setInstance(userContext);
    }

    public static void removeUserContext() {
        UserContext.removeInstance();
    }
}
