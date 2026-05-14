package one.xis.context;


import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

class ActiveProfiles {

    private static final Set<String> PROFILES;

    static {
        Set<String> profileSet = new HashSet<>();
        profileSet.addAll(parseProfiles(systemPropertyProfiles()));
        profileSet.addAll(parseProfiles(envVariableProfiles()));
        PROFILES = Collections.unmodifiableSet(profileSet);
    }

    public static Set<String> getProfiles() {
        return PROFILES;
    }

    private static Set<String> parseProfiles(String profilesString) {
        Set<String> profilesSet = new HashSet<>();
        if (profilesString != null && !profilesString.isBlank()) {
            String[] profilesArray = profilesString.split(",");
            for (String profile : profilesArray) {
                profilesSet.add(profile.trim());
            }
        }
        return profilesSet;
    }

    private static String systemPropertyProfiles() {
        return System.getProperty("xis.profiles");
    }

    private static String envVariableProfiles() {
        return System.getenv("XIS_PROFILES");
    }

}
