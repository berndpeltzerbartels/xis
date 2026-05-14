package one.xis.plugin;

import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.compile.JavaCompile;
import org.gradle.api.tasks.options.Option;
import org.gradle.process.CommandLineArgumentProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs javac with -proc:only to execute the XIS validation processor.
 */
public class XISValidateTask extends JavaCompile {

    private final Property<String> processorFqcn = getProject().getObjects().property(String.class);
    private boolean allErrors;

    public XISValidateTask() {
        getDestinationDirectory().set(getProject().getLayout().getBuildDirectory().dir("tmp/xis-validate-classes"));

        getOptions().getCompilerArgumentProviders().add(new CommandLineArgumentProvider() {
            @Override
            public Iterable<String> asArguments() {
                List<String> args = new ArrayList<>(4);
                args.add("-proc:only");
                args.add("-processor");
                args.add(getProcessorFqcn().get());
                args.add("-Axis.projectDir=" + getProject().getProjectDir().getAbsolutePath());
                if (allErrors) {
                    args.add("-Axis.allErrors=true");
                }
                return args;
            }
        });
    }

    @Internal
    public Property<String> getProcessorFqcn() {
        return processorFqcn;
    }

    @Input
    public boolean isAllErrors() {
        return allErrors;
    }

    @Option(option = "all-errors", description = "Report all XIS validation errors instead of stopping after the first one.")
    public void setAllErrors(boolean allErrors) {
        this.allErrors = allErrors;
    }
}
