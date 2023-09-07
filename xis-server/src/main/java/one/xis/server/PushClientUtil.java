package one.xis.server;

import one.xis.EnablePushClients;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class PushClientUtil {
    
    public static Set<String> packagesToScanForPushClients(EnablePushClients enablePushClients) {
        var packages = new HashSet<String>();
        Collections.addAll(packages, enablePushClients.basePackages());
        Arrays.stream(enablePushClients.basePackageClasses()).map(Class::getPackageName).forEach(packages::add);
        return packages;
    }


}
