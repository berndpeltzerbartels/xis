package one.xis.micronaut

import io.micronaut.context.annotation.Import
import io.micronaut.runtime.Micronaut

@Import(packages = "one.xis.jscomponent", annotated = "*")
class MicronautTestApplication {
    static void main(String[] args) {
        Micronaut.run(MicronautTestApplication, args)
    }
}
