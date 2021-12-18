package one.xis.remote.example.project;

import one.xis.remote.*;

import java.util.List;

@RemoteService
public abstract class ProjectListRemoteService {

    private ProjectService projectService;

    @RemoteMethod
    void updateList(@RemoteUser String userId) {
        updateProjectList(projectService.getProjectList(userId), userId);
    }

    @RemoteState("/projectList")
    @RemoteHandler("onProjectListUpdated")
    abstract void updateProjectList(List<Project> projects, @RemoteUser String userId);

}
