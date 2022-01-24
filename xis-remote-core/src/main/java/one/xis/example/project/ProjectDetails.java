package one.xis.example.project;

import one.xis.example.MainPage;
import one.xis.remote.*;

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

    @OnEvent
    void onEditClicked(MainPage mainPage, ProjectForm projectForm) {
        projectService.getProject(id, userId);
        mainPage.setMainContent(projectForm);
    }

    public void setProject(Project project) {
        this.id = project.getId();
        this.name = project.getName();
    }
}
