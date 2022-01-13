package one.xis.example.project;

import one.xis.*;
import one.xis.example.MainPage;

import java.util.List;

@Widget
public class ProjectList {

    private ProjectService projectService;

    @Variable
    private List<Project> projects;

    @UserId
    private String userId;

    @Widget
    private MainPage mainPage;

    @Widget
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
