package one.xis.gradle;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

class JSFileSorter {

    static List<JSFile> sort(Collection<JSFile> files) {
        var rv = new ArrayList<JSFile>();
        var declaredClassNames = new HashSet<String>();
        var filesLeft = new HashSet<>(files);
        var matches = new HashSet<JSFile>();
        while (!filesLeft.isEmpty()) {
            for (JSFile file : filesLeft) {
                if (declaredClassNames.containsAll(file.getSuperClasses())) {
                    matches.add(file);
                }
            }
            if (matches.isEmpty()) {
                throw new IllegalStateException("circular or illegal dependencies, envolved files: " + asList(filesLeft));
            }
            filesLeft.removeAll(matches);
            var classNames = matches.stream()
                    .map(JSFile::getDeclaredClasses)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toSet());
            declaredClassNames.addAll(classNames);
            rv.addAll(matches);
            matches.clear();
        }
        return rv;
    }

    private static String asList(Collection<JSFile> files) {
        return files.stream().map(JSFile::getFile)
                .map(File::getName)
                .collect(Collectors.joining(", "));
    }
}
