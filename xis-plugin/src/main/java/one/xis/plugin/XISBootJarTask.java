package one.xis.plugin;

import org.gradle.api.GradleException;
import org.gradle.api.Project;
import org.gradle.api.file.DuplicatesStrategy;
import org.gradle.api.tasks.SourceSet;
import org.gradle.api.tasks.SourceSetContainer;
import org.gradle.api.tasks.bundling.Jar;

import java.util.stream.Collectors;

public abstract class XISBootJarTask extends Jar {

    private String runnerMainClass = "one.xis.boot.Runner";
    private String runnerClassPath = "one/xis/boot/Runner.class";
    private String runnerErrorMessage = "xisJar requires exactly one application class annotated with @XISBootApplication. "
            + "The annotation processor generates one.xis.boot.Runner from that class.";

    public XISBootJarTask() {
        setGroup("xis");
        setDescription("Creates an executable JAR with all dependencies.");
        setDuplicatesStrategy(DuplicatesStrategy.EXCLUDE);

    }

    public void configure(Project project) {
        getManifest().getAttributes().put("Main-Class", runnerMainClass);

        SourceSetContainer sets = project.getExtensions().getByType(SourceSetContainer.class);
        SourceSet main = sets.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
        var buildDirectory = project.getLayout().getBuildDirectory();

        doFirst(task -> requireGeneratedRunner(main));

        from(main.getOutput());

        from(
                project.getConfigurations().getByName("runtimeClasspath")
                        .filter(file -> file.getName().endsWith(".jar"))
                        .getFiles()
                        .stream()
                        .map(project::zipTree)
                        .collect(Collectors.toList())
        );
    }

    private void requireGeneratedRunner(SourceSet main) {
        boolean runnerExists = main.getOutput().getClassesDirs().getFiles().stream()
                .anyMatch(classesDir -> classesDir.toPath()
                        .resolve(runnerClassPath)
                        .toFile()
                        .isFile());
        if (!runnerExists) {
            throw new GradleException(runnerErrorMessage);
        }
    }

    public void useRunner(String runnerMainClass, String runnerClassPath, String runnerErrorMessage) {
        this.runnerMainClass = runnerMainClass;
        this.runnerClassPath = runnerClassPath;
        this.runnerErrorMessage = runnerErrorMessage;
        getManifest().getAttributes().put("Main-Class", runnerMainClass);
    }

}
