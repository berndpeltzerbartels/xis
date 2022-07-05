package micronaut.example

import io.micronaut.context.annotation.Import
import io.micronaut.runtime.Micronaut

@Import(packages = "one.xis.jsc", annotated = "*")
class MicronautTestApplication {
    static void main(String[] args) {
        Micronaut.run(MicronautTestApplication, args)
    }
}
