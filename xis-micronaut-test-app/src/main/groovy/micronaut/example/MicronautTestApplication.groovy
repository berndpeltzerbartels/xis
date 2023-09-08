package micronaut.example

import io.micronaut.context.annotation.Import
import io.micronaut.runtime.Micronaut
import jakarta.inject.Singleton
import one.xis.EnablePushClients

@EnablePushClients(basePackageClasses = Index)
@Import(packages = ['one.xis.micronaut'], annotated = '*')
@Singleton
class MicronautTestApplication {

    static void main(String[] args) {
        Micronaut.run(MicronautTestApplication, args)
    }
}
