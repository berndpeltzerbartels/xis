package one.xis.gradle;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.tasks.SourceSet;

public class XISPlugin implements Plugin<Project> {

    @Override
    public void apply(Project project) {
        System.out.println(project.getExtensions().getByType(JavaPluginExtension.class)
                .getSourceSets().getByName(SourceSet.MAIN_SOURCE_SET_NAME));

    }


}
