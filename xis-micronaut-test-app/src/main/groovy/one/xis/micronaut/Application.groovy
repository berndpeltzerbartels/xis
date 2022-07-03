package one.xis.micronaut

import io.micronaut.context.annotation.Import
import io.micronaut.runtime.Micronaut

@Import(packages = "one.xis.jscomponent", annotated = "*")
class Application {
    static void main(String[] args) {
        Micronaut.run(Application, args)
    }
}
