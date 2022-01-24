package one.xis.example.project;

import one.xis.example.MainPage;
import one.xis.example.user.User;
import one.xis.example.user.UserService;
import one.xis.remote.*;

import java.util.ArrayList;
import java.util.List;

@Widget
public class ProjectForm {

    private ProjectService projectService;

    private UserService userService;

    @UserId
    private String userId;

    @Variable
    @Identifier
    private Long projectId;

    @Variable
    private String projectName;

    @Variable
    private List<User> users;

    @Method
    void save(MainPage mainPage, ProjectDetails projectDetails) {
        if (validate()) {
            saveAndRedirect(mainPage, projectDetails);
        }
    }

    private void saveAndRedirect(MainPage mainPage, ProjectDetails projectDetails) {
        Project project = projectService.getProject(projectId, userId);
        project.setName(projectName);
        projectService.save(project);
        projectDetails.setProject(project);
        mainPage.setMainContent(projectDetails);
        mainPage.setMessage("Ihre Änderungen wurden gespeichert");
    }

    private boolean validate() {
        List<String> messages = new ArrayList<>();
        ValidationUtil.validate(projectName, name -> !name.isBlank(), "Der Name darf nicht leer sein", messages);
        ValidationUtil.validate(projectName, name -> name.length() < 51, "Der Name darf maximal 50 Zeichen haben", messages);
        return messages.isEmpty();
    }


}
