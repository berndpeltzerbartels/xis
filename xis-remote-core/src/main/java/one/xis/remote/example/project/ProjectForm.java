package one.xis.remote.example.project;

import one.xis.remote.*;
import one.xis.remote.example.MainPage;
import one.xis.remote.example.user.User;
import one.xis.remote.example.user.UserService;

import java.util.ArrayList;
import java.util.List;

@Container
public class ProjectForm {

    private ProjectService projectService;

    private UserService userService;

    @Container
    private MainPage mainPage;

    @Container
    private ProjectDetails projectDetails;

    @UserId
    private String userId;

    @Binding
    @Identifier
    private Long projectId;

    @Binding
    private String projectName;

    @Binding
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
