package one.xis.context.all;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import one.xis.context.Component;

@Getter
@Component
@RequiredArgsConstructor
class AdvisedServiceImpl implements AdvisedService {
    private final Comp2 comp2;

    @OverallTimed
    public String describe() {
        return "service:" + comp2.getClass().getSimpleName();
    }
}
