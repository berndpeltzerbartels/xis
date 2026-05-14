package one.xis.deserialize

import one.xis.FormData
import one.xis.UserContext
import one.xis.context.TestContextBuilder
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

import java.time.ZoneId

import static org.assertj.core.api.Assertions.assertThat
import static org.mockito.Mockito.mock
import static org.mockito.Mockito.when

class GroovyDeserializationTest {

    private MainDeserializer mainDeserializer
    private UserContext userContext

    @BeforeEach
    void init() {
        def context = new TestContextBuilder()
                .withPackage('one.xis.deserialize')
                .build()
        mainDeserializer = context.getSingleton(MainDeserializer)
        userContext = mock(UserContext)
        when(userContext.getLocale()).thenReturn(Locale.GERMAN)
        when(userContext.getZoneId()).thenReturn(ZoneId.of('Europe/Berlin'))
    }

    @Test
    void deserializesGroovyObjectFromMethodParameter() {
        def parameter = getClass().getDeclaredMethod('groovyForm', GroovyForm).parameters[0]
        def form = mainDeserializer.deserialize('{"name":"Bernd","level":7}', parameter, userContext, new PostProcessingResults()) as GroovyForm

        assertThat(form.name).isEqualTo('Bernd')
        assertThat(form.level).isEqualTo(7)
    }

    void groovyForm(@FormData('form') GroovyForm form) {
    }
}

class GroovyForm {
    String name
    int level
}
