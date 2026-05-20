package one.xis.plugin;

import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Classpath;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;
import org.gradle.api.DefaultTask;

import java.io.File;
import java.util.LinkedHashSet;
import java.util.Locale;

public abstract class XISNativeCompileTask extends DefaultTask {

    private final ConfigurableFileCollection nativeClasspath = getProject().files();
    private final Property<String> graalVmHome = getProject().getObjects().property(String.class);

    public XISNativeCompileTask() {
        setGroup("xis");
        setDescription("Compiles the XIS Boot Native application with GraalVM native-image.");
    }

    @Classpath
    public ConfigurableFileCollection getNativeClasspath() {
        return nativeClasspath;
    }

    @Input
    @Optional
    public Property<String> getGraalVmHome() {
        return graalVmHome;
    }

    @Input
    public abstract Property<String> getMainClass();

    @InputFile
    public abstract RegularFileProperty getReflectionConfig();

    @InputFile
    public abstract RegularFileProperty getProxyConfig();

    @OutputFile
    public abstract RegularFileProperty getExecutableFile();

    @Option(option = "graal-vm-home", description = "Path to the GraalVM home used for native-image.")
    public void setGraalVmHome(String graalVmHome) {
        getGraalVmHome().set(graalVmHome);
    }

    @TaskAction
    public void compileNativeImage() {
        var nativeImage = findNativeImage();

        var outputFile = getExecutableFile().get().getAsFile();
        outputFile.getParentFile().mkdirs();

        var classpathFiles = new LinkedHashSet<File>();
        classpathFiles.addAll(nativeClasspath.getFiles());
        var classpath = classpathFiles.stream()
                .filter(file -> file != null
                        && file.exists()
                        && !file.getName().startsWith("graal-sdk-")
                        && !file.getName().startsWith("svm-")
                        && !file.getName().contains("tinylog"))
                .map(File::getAbsolutePath)
                .reduce((left, right) -> left + File.pathSeparator + right)
                .orElseThrow(() -> new GradleException("Native classpath is empty."));

        getProject().exec(exec -> {
            exec.executable(nativeImage.getAbsolutePath());
            exec.args(
                    "--no-fallback",
                    "--initialize-at-build-time=org.slf4j",
                    "--initialize-at-run-time=one.xis.context.ApplicationProperties",
                    "--initialize-at-run-time=org.mariadb.jdbc",
                    "-H:IncludeResources=.*\\.(html|css|js|properties|json|txt)$",
                    "-H:ReflectionConfigurationFiles=" + getReflectionConfig().get().getAsFile().getAbsolutePath(),
                    "-H:DynamicProxyConfigurationFiles=" + getProxyConfig().get().getAsFile().getAbsolutePath(),
                    "-cp", classpath,
                    getMainClass().get(),
                    outputFile.getAbsolutePath()
            );
        });
    }

    private File findNativeImage() {
        var configured = getGraalVmHome().getOrNull();
        if (configured != null && !configured.isBlank()) {
            return requireNativeImage(new File(configured, "bin/native-image"), "configured GraalVM home");
        }

        for (String environmentVariable : new String[]{"GRAALVM_HOME", "JAVA_HOME"}) {
            var home = System.getenv(environmentVariable);
            if (home != null && !home.isBlank()) {
                var nativeImage = new File(home, "bin/native-image");
                if (nativeImage.exists()) {
                    return nativeImage;
                }
            }
        }

        var pathNativeImage = findOnPath();
        if (pathNativeImage != null) {
            return pathNativeImage;
        }

        for (File home : candidateHomes()) {
            var nativeImage = new File(home, "bin/native-image");
            if (nativeImage.exists()) {
                return nativeImage;
            }
        }

        throw new GradleException("native-image not found. Install GraalVM with native-image and set "
                + "-PgraalVmHome=/path/to/graalvm, GRAALVM_HOME, JAVA_HOME, or use --graal-vm-home.");
    }

    private static File requireNativeImage(File nativeImage, String source) {
        if (!nativeImage.exists()) {
            throw new GradleException("native-image not found at " + nativeImage + " from " + source + ".");
        }
        return nativeImage;
    }

    private static File findOnPath() {
        var path = System.getenv("PATH");
        if (path == null || path.isBlank()) {
            return null;
        }
        for (String entry : path.split(File.pathSeparator)) {
            var nativeImage = new File(entry, "native-image");
            if (nativeImage.exists() && nativeImage.isFile()) {
                return nativeImage;
            }
        }
        return null;
    }

    private static LinkedHashSet<File> candidateHomes() {
        var homes = new LinkedHashSet<File>();
        addJavaHomeCandidates(homes, new File(System.getProperty("user.home"), ".sdkman/candidates/java"));
        addJavaHomeCandidates(homes, new File("/Library/Java/JavaVirtualMachines"));
        addJavaHomeCandidates(homes, new File("/usr/lib/jvm"));
        return homes;
    }

    private static void addJavaHomeCandidates(LinkedHashSet<File> homes, File baseDir) {
        var children = baseDir.listFiles(File::isDirectory);
        if (children == null) {
            return;
        }
        for (File child : children) {
            var name = child.getName().toLowerCase(Locale.ROOT);
            if (!name.contains("graal")) {
                continue;
            }
            homes.add(child);
            homes.add(new File(child, "Contents/Home"));
        }
    }
}
