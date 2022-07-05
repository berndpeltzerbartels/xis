package spring.example;

import lombok.Data;
import one.xis.ClientState;

import java.util.List;

@Data
@ClientState
public class ProjectState {
    private List<Project> projects;
}
