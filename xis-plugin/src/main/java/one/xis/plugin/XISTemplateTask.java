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
 * Output: build/resources/main
 */
public class XISTemplateTask extends JavaCompile {

    // Fixed by the plugin; not user-configurable
    private final Property<String> processorFqcn = getProject().getObjects().property(String.class);
    private final DirectoryProperty outputDir = getProject().getObjects().directoryProperty();

    public XISTemplateTask() {
        // We do not produce class files; put javac "classes" into a tmp folder
        getDestinationDirectory().set(getProject().getLayout().getBuildDirectory().dir("tmp/xis-templates-classes"));

        // Provide compiler args lazily & correctly
        getOptions().getCompilerArgumentProviders().add(new CommandLineArgumentProvider() {
            @Override
            public Iterable<String> asArguments() {
                File out = outputDir.get().getAsFile();
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

    @Internal
    public Property<String> getProcessorFqcn() {
        return processorFqcn;
    }

    @Internal
    public DirectoryProperty getOutputDir() {
        return outputDir;
    }
}
