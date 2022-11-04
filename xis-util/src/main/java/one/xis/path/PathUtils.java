package one.xis.path;

import lombok.NonNull;
import lombok.experimental.UtilityClass;
import one.xis.utils.lang.StringUtils;

@UtilityClass
public class PathUtils {

    public String getSuffix(String path) {
        String file = getFile(path);
        int j = file.lastIndexOf('.');
        return j == -1 ? null : file.substring(j + 1);
    }

    public String stripSuffix(String path) {
        int i = path.lastIndexOf('/');
        int j = path.lastIndexOf('.');
        if (j == -1) {
            return path;
        }
        if (i > j) {
            return path;
        }
        return path.substring(0, j);
    }
    
    public boolean hasSuffix(String path) {
        int i = path.lastIndexOf('/');
        int j = path.lastIndexOf('.');
        if (j == -1) {
            return false;
        }
        if (i > j) {
            return false;
        }
        return true;
    }

    public String stripTrailingSlash(String path) {
        if (path.startsWith("/")) {
            return path.substring(1);
        }
        return path;
    }


    public String getFile(String path) {
        int i = path.lastIndexOf('/');
        return i == -1 ? path : path.substring(i + 1);
    }

    public String appendPath(@NonNull String path1, @NonNull String path2) {
        StringBuilder builder = new StringBuilder();
        if (path1.endsWith("/")) {
            builder.append(StringUtils.removeLastChar(path1));
        } else {
            builder.append(path1);
        }
        builder.append("/");
        if (path2.startsWith("/")) {
            builder.append(StringUtils.removeFirstChar(path2));
        } else {
            builder.append(path2);
        }
        return builder.toString();

    }
}
