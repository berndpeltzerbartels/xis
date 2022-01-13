package one.xis.example.project;

import one.xis.*;
import one.xis.example.MainPage;
import one.xis.example.user.User;
import one.xis.example.user.UserService;

import java.util.ArrayList;
import java.util.List;

@Widget
public class ProjectForm {

    private ProjectService projectService;

    private UserService userService;

    @Widget
    private MainPage mainPage;

    @Widget
    private ProjectDetails projectDetails;

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
    void save() {
        if (validate()) {
            saveAndRedirect();
        }
    }

    private void saveAndRedirect() {
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
