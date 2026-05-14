package one.xis.gradle;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Collection;
import java.util.HashSet;

class FileUtils {

    static String getContent(File file, String encoding) {
        try {
            return new String(Files.readAllBytes(file.toPath()), encoding);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static Collection<File> files(File dir, String suffix) {
        var result = new HashSet<File>();
        evalDir(dir, result, suffix);
        return result;
    }

    private static void evalDir(File dir, Collection<File> result, String suffix) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (var file : files) {
                if (file.isDirectory()) {
                    evalDir(file, result, suffix);
                } else if (file.getName().endsWith("." + suffix)) {
                    result.add(file);
                }
            }
        }
    }
}
