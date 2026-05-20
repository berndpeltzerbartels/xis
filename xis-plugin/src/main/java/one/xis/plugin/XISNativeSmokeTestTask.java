package one.xis.plugin;

import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.InputFile;
import org.gradle.api.tasks.TaskAction;
import org.gradle.api.tasks.options.Option;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

public abstract class XISNativeSmokeTestTask extends DefaultTask {

    private final Property<Integer> port = getProject().getObjects().property(Integer.class);

    public XISNativeSmokeTestTask() {
        setGroup("verification");
        setDescription("Starts the XIS Boot Native executable and verifies that it serves the root page.");
    }

    @InputFile
    public abstract RegularFileProperty getExecutableFile();

    @Input
    public Property<Integer> getPort() {
        return port;
    }

    @Option(option = "port", description = "Server port used for the native smoke test.")
    public void setPort(String port) {
        getPort().set(Integer.parseInt(port));
    }

    @TaskAction
    public void smokeTest() throws Exception {
        var executable = getExecutableFile().get().getAsFile();
        if (!executable.exists()) {
            throw new GradleException("Native executable not found: " + executable);
        }

        var process = new ProcessBuilder(executable.getAbsolutePath(), String.valueOf(getPort().get()))
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                .start();
        try {
            waitForServer(process, getPort().get());
            var response = httpGet("http://localhost:" + getPort().get() + "/");
            if (response.isBlank()) {
                throw new GradleException("Native smoke test received an empty root page.");
            }
        } finally {
            process.destroy();
            if (!process.waitFor(3, TimeUnit.SECONDS)) {
                process.destroyForcibly();
                process.waitFor();
            }
        }
    }

    private static void waitForServer(Process process, int port) throws Exception {
        var deadline = System.currentTimeMillis() + 15_000;
        while (System.currentTimeMillis() < deadline) {
            if (!process.isAlive()) {
                throw new GradleException("Native server exited before it was reachable.");
            }
            try {
                httpGet("http://localhost:" + port + "/");
                return;
            } catch (IOException ignored) {
                Thread.sleep(200);
            }
        }
        throw new GradleException("Native server did not become reachable on port " + port + ".");
    }

    private static String httpGet(String url) throws IOException {
        var connection = new URL(url).openConnection();
        connection.setConnectTimeout(2_000);
        connection.setReadTimeout(5_000);
        try (var stream = connection.getInputStream()) {
            return new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        }
    }
}
