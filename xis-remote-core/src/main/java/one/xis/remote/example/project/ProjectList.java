package one.xis.remote.example.project;

import one.xis.remote.*;
import one.xis.remote.example.MainPage;

import java.util.List;

@Container
public class ProjectList {

    private ProjectService projectService;

    @Binding
    private List<Project> projects;

    @UserId
    private String userId;

    @Container
    private MainPage mainPage;

    @Container
    private ProjectDetails projectDetails;

    @OnInit
    void init(@UserId String userId) {
        projects = projectService.getProjectList(userId);
    }

    @Method
    void onProjectClicked(@Param long projectId) {
        Project project = projectService.getProject(projectId, userId);
        projectDetails.setProject(project);
        mainPage.setMainContent(projectDetails);
    }
}
