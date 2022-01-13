package one.xis.example.project;

import one.xis.*;
import one.xis.example.MainPage;

@Widget
public abstract class ProjectDetails {

    private ProjectService projectService;

    @Variable
    private String name;

    @Variable
    @Identifier
    private Long id;

    @UserId
    private String userId;

    @Widget
    private ProjectForm projectForm;

    @OnEvent
    void onEditClicked(MainPage index) {
        projectService.getProject(id, userId);
        index.setMainContent(projectForm);
    }

    public void setProject(Project project) {
        this.id = project.getId();
        this.name = project.getName();
    }
}
