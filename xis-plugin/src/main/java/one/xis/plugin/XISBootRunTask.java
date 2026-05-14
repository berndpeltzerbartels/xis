package one.xis.plugin;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.JavaExec;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.options.Option;

import java.util.ArrayList;
import java.util.List;

public abstract class XISBootRunTask extends JavaExec {

    private final List<String> applicationArgs = new ArrayList<>();
    private String port;

    public XISBootRunTask() {
        setGroup("xis");
        setDescription("Builds and starts the XIS Boot jar.");
        getMainClass().set("one.xis.boot.Runner");
    }

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getJarFile();

    @Option(option = "port", description = "Server port passed as first argument to XIS Boot.")
    public void setPort(String port) {
        this.port = port;
    }

    @Option(option = "args", description = "Additional arguments passed to the XIS Boot application.")
    public void setArgs(String args) {
        applicationArgs.clear();
        if (!args.isBlank()) {
            applicationArgs.addAll(List.of(args.split("\\s+")));
        }
    }

    @Override
    public void exec() {
        classpath(getJarFile());
        setArgs(applicationArguments());
        super.exec();
    }

    private List<String> applicationArguments() {
        var result = new ArrayList<String>();
        if (port != null && !port.isBlank()) {
            result.add(port);
        }
        result.addAll(applicationArgs);
        return result;
    }
}
