package one.xis.sql.proxyapp;

import one.xis.context.Component;

@Component
public class ContextBuildServiceImpl implements ContextBuildService {
    private final ContextBuildRepository repository;

    ContextBuildServiceImpl(ContextBuildRepository repository) {
        this.repository = repository;
    }

    @Override
    public String nameById(long id) {
        return repository.nameById(id);
    }
}
