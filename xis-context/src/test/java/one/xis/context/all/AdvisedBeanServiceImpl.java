package one.xis.context.all;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
class AdvisedBeanServiceImpl implements AdvisedBeanService {
    private final Comp4 comp4;

    @OverallTimed
    public String describe() {
        return "bean:" + comp4.getClass().getSimpleName();
    }
}
