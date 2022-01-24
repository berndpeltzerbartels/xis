package one.xis.example.project;

import one.xis.example.MainPage;
import one.xis.remote.*;

import java.util.List;

@Widget
public class ProjectList {

    private ProjectService projectService;

    @ClientState
    private List<Project> projects;

    @UserId
    private String userId;

    @OnInit
    void init(@UserId String userId) {
        projects = projectService.getProjectList(userId);
    }

    @Method
    void onProjectClicked(@Param long projectId, MainPage mainPage, ProjectDetails projectDetails) {
        Project project = projectService.getProject(projectId, userId);
        projectDetails.setProject(project);
        mainPage.setMainContent(projectDetails);
    }
}
