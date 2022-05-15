package one.xis.gradle;

import lombok.experimental.UtilityClass;

import java.io.File;

@UtilityClass
class FileUtil {

    String relativePath(File childFile, File directory) {
        if (childFile.getAbsolutePath().startsWith(directory.getAbsolutePath())) {
            return childFile.getAbsolutePath().substring(directory.getAbsolutePath().length() + 1);
        }
        throw new IllegalArgumentException(directory.getAbsolutePath() + " does not contain " + childFile.getAbsolutePath());
    }

    File newSuffix(File file, String newSuffix) {
        String name = file.getName();
        int index = file.getName().lastIndexOf('.');
        if (index == -1) {
            return new File(file.getParentFile(), name + newSuffix);
        }
        return new File(file.getParentFile(), name.substring(0, index) + newSuffix);
    }

    String removeSuffix(String fileName) {
        int index = fileName.lastIndexOf('.');
        if (index == -1) {
            throw new IllegalArgumentException("no suffix: " + fileName);
        }
        return fileName.substring(0, index);
    }

}

