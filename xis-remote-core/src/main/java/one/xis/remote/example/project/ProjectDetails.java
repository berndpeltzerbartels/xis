package one.xis.remote.example.project;

import one.xis.remote.*;
import one.xis.remote.example.MainPage;

@PageComponent
public abstract class ProjectDetails {

    private ProjectService projectService;

    @Binding
    private String name;

    @Binding
    @Identifier
    private Long id;

    @UserId
    private String userId;

    @PageComponent
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
