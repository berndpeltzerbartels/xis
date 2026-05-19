package one.xis.plugin;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.tasks.Exec;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.options.Option;

import java.util.ArrayList;
import java.util.List;

public abstract class XISNativeRunTask extends Exec {

    private String port;
    private final List<String> applicationArgs = new ArrayList<>();

    public XISNativeRunTask() {
        setGroup("xis");
        setDescription("Runs the compiled XIS Boot Native executable.");
    }

    @InputFile
    @PathSensitive(PathSensitivity.NONE)
    public abstract RegularFileProperty getExecutableFile();

    @Option(option = "port", description = "Server port passed as first argument to XIS Boot Native.")
    public void setPort(String port) {
        this.port = port;
    }

    @Option(option = "application-args", description = "Additional arguments passed to the native application.")
    public void setApplicationArgs(String args) {
        applicationArgs.clear();
        if (args != null && !args.isBlank()) {
            applicationArgs.addAll(List.of(args.split("\\s+")));
        }
    }

    @Override
    protected void exec() {
        executable(getExecutableFile().get().getAsFile().getAbsolutePath());
        args(applicationArguments());
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
