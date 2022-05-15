package one.xis.gradle;

import lombok.Getter;

import java.io.File;

@Getter
class HtmlJavaPair {

    private final File htmlFile;
    private final File javaFile;
    private final File sourceRoot;
    private final String relativePathJavaFile;
    private final String packageName;
    private final String javaClassSimpleName;

    private HtmlJavaPair(File htmlFile, File sourceRoot) {
        this.htmlFile = htmlFile;
        this.sourceRoot = sourceRoot;
        this.javaFile = FileUtil.newSuffix(htmlFile, ".java");
        this.relativePathJavaFile = FileUtil.relativePath(javaFile, sourceRoot);
        this.packageName = packageName(FileUtil.relativePath(javaFile.getParentFile(), sourceRoot));
        this.javaClassSimpleName = FileUtil.removeSuffix(javaFile.getName());
    }

    static HtmlJavaPair fromHtmlFile(File htmlFile, File sourceRoot) {
        return new HtmlJavaPair(htmlFile, sourceRoot);
    }

    boolean complete() {
        return htmlFile.exists() && javaFile.exists();
    }

    private static String packageName(String relativePath) {
        return relativePath.replace('/', '.');
    }

}
