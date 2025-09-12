package one.xis.plugin;

import org.gradle.api.file.Directory;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.options.Option;
import org.gradle.process.CommandLineArgumentProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Runs javac with -proc:only to execute the XIS annotation processor.
 * Default output: src/main/java
 * Flag --useResources switches to src/main/resources.
 */
public class XISTemplateTask extends JavaCompile {

    // Fixed by the plugin; not user-configurable
    private final Property<String> processorFqcn = getProject().getObjects().property(String.class);
    private final DirectoryProperty defaultJavaOutputDir = getProject().getObjects().directoryProperty();
    private final DirectoryProperty resourcesOutputDir = getProject().getObjects().directoryProperty();

    // Only configurable flag (CLI): --useResources
    private final Property<Boolean> useResources = getProject().getObjects().property(Boolean.class).convention(false);

    public XISTemplateTask() {
        // We do not produce class files; put javac "classes" into a tmp folder
        getDestinationDirectory().set(getProject().getLayout().getBuildDirectory().dir("tmp/xis-templates-classes"));

        // Provider for the effective output dir, chosen by the flag
        Provider<Directory> outDir =
                getUseResources().flatMap(flag -> flag ? getResourcesOutputDir() : getDefaultJavaOutputDir());

        // Declare outputs at configuration time (important!)
        getOutputs().dir(outDir);

        // Provide compiler args lazily & correctly
        getOptions().getCompilerArgumentProviders().add(new CommandLineArgumentProvider() {
            @Override
            public Iterable<String> asArguments() {
                File out = outDir.get().getAsFile();
                if (!out.exists()) { // safe at execution time
                    //noinspection ResultOfMethodCallIgnored
                    out.mkdirs();
                }
                List<String> args = new ArrayList<>(6);
                args.add("-proc:only");
                args.add("-processor");
                args.add(getProcessorFqcn().get());
                args.add("-Axis.outputDir=" + out.getAbsolutePath());
                return args;
            }
        });
    }

    /* ---- Only flag is an @Input; the rest is @Internal (plugin-controlled) ---- */

    @Option(option = "useResources",
            description = "Generate under src/main/resources (default: src/main/java).")
    public void setUseResources(boolean flag) {
        this.useResources.set(flag);
    }

    @Input
    public Property<Boolean> getUseResources() {
        return useResources;
    }

    @Internal
    public Property<String> getProcessorFqcn() {
        return processorFqcn;
    }

    @Internal
    public DirectoryProperty getDefaultJavaOutputDir() {
        return defaultJavaOutputDir;
    }

    @Internal
    public DirectoryProperty getResourcesOutputDir() {
        return resourcesOutputDir;
    }
}
