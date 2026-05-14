package one.xis.server;

public class UserTargetingNotAvailableException extends IllegalStateException {

    public UserTargetingNotAvailableException() {
        super("User-targeted refresh events require the XIS authentication module. " +
                "Add 'one.xis:xis-authentication' to your application or publish to client targets instead.");
    }
}
