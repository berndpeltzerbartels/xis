package one.xis.plugin;

import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Internal;
import org.gradle.api.tasks.compile.GroovyCompile;
import org.gradle.process.CommandLineArgumentProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class XISGroovyTestTask extends GroovyCompile {

    private final Property<String> processorFqcn = getProject().getObjects().property(String.class);
    private final DirectoryProperty outputDir = getProject().getObjects().directoryProperty();

    public XISGroovyTestTask() {
        getDestinationDirectory().set(getProject().getLayout().getBuildDirectory().dir("tmp/xis-groovy-tests-classes"));
        getGroovyOptions().setJavaAnnotationProcessing(true);

        getOptions().getCompilerArgumentProviders().add(new CommandLineArgumentProvider() {
            @Override
            public Iterable<String> asArguments() {
                File out = outputDir.get().getAsFile();
                if (!out.exists()) {
                    //noinspection ResultOfMethodCallIgnored
                    out.mkdirs();
                }
                List<String> args = new ArrayList<>(4);
                args.add("-processor");
                args.add(getProcessorFqcn().get());
                args.add("-Axis.testOutputDir=" + out.getAbsolutePath());
                args.add("-Axis.testLanguage=groovy");
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
