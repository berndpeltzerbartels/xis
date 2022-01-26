package one.xis.test;

import lombok.Data;
import one.xis.remote.ClientState;

import java.util.List;

@Data
@ClientState
public class ProjectState {
    private List<Project> projects;
}
