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

        // 1. Sonderfall: Functions-Dateien zuerst
        var functionFiles = files.stream()
                .filter(file -> file.getFile().getAbsolutePath().contains("functions/"))
                .collect(Collectors.toList());

        // Safety: functions.js darf keine Klassen enthalten
        for (var f : functionFiles) {
            if (!f.getDeclaredClasses().isEmpty()) {
                throw new IllegalStateException("functions.js must not declare classes: " + f.getFile().getName());
            }
        }

        rv.addAll(functionFiles);

        // 2. Übrige Dateien behandeln wie bisher (Klassenabhängigkeiten)
        var classFiles = files.stream()
                .filter(f -> !functionFiles.contains(f))
                .collect(Collectors.toSet());

        rv.addAll(sortedClassFiles(classFiles));
        return rv;
    }


    private static List<JSFile> sortedClassFiles(Collection<JSFile> classFiles) {
        var rv = new ArrayList<JSFile>();
        var declaredClassNames = new HashSet<String>();
        var filesLeft = new HashSet<>(classFiles);
        var matches = new HashSet<JSFile>();
        while (!filesLeft.isEmpty()) {
            for (JSFile file : filesLeft) {
                if (declaredClassNames.containsAll(file.getSuperClasses())) {
                    matches.add(file);
                }
            }
            if (matches.isEmpty()) {
                throw new IllegalStateException("circular or illegal dependencies, involved files: " + asList(filesLeft));
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
