package one.xis;

import java.time.ZoneId;
import java.util.Locale;
import java.util.Set;

final class UserContextHolder {

    private static final ThreadLocal<UserContext> USER_CONTEXT = ThreadLocal.withInitial(EmptyUserContext::new);

    private UserContextHolder() {
    }

    static UserContext getInstance() {
        return USER_CONTEXT.get();
    }

    static void setInstance(UserContext userContext) {
        USER_CONTEXT.set(userContext);
    }

    static void clear() {
        USER_CONTEXT.remove();
    }

    private static final class EmptyUserContext implements UserContext {

        @Override
        public Locale getLocale() {
            return Locale.getDefault();
        }

        @Override
        public ZoneId getZoneId() {
            return ZoneId.systemDefault();
        }

        @Override
        public String getClientId() {
            return null;
        }

        @Override
        public String getUserId() {
            return null;
        }

        @Override
        public Set<String> getRoles() {
            return Set.of();
        }

        @Override
        public boolean isAuthenticated() {
            return false;
        }
    }
}
