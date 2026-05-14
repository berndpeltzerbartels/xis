package test.page.groovy

import one.xis.Action
import one.xis.FormData
import one.xis.HtmlFile
import one.xis.ModelData
import one.xis.Page
import one.xis.PathVariable
import one.xis.QueryParameter
import one.xis.context.IntegrationTestContext
import one.xis.validation.Mandatory
import org.junit.jupiter.api.Test

import static org.assertj.core.api.Assertions.assertThat

class GroovyPageIntegrationTest {

    @Test
    void groovyPageCanRenderModelDataAndInvokeActionWithDeserializedFormData() {
        def context = IntegrationTestContext.builder()
                .withSingleton(GroovyPage)
                .build()

        def client = context.openPage('/groovy/42.html?mode=create')
        def document = client.document

        assertThat(document.getElementById('headline').innerText).isEqualTo('Item 42 create')
        assertThat(document.getInputElementById('name').value).isEqualTo('initial')
        assertThat(document.getInputElementById('amount').value).isEqualTo('3')

        document.getInputElementById('name').setValue('submitted')
        document.getInputElementById('amount').setValue('9')
        document.getElementById('save').click()

        assertThat(document.getElementById('saved').innerText).isEqualTo('submitted:9')
    }

    @Test
    void groovyFormDataParticipatesInValidation() {
        def context = IntegrationTestContext.builder()
                .withSingleton(GroovyPage)
                .build()

        def client = context.openPage('/groovy/42.html?mode=create')
        def document = client.document

        document.getInputElementById('name').setValue('')
        document.getInputElementById('amount').setValue('abc')
        document.getElementById('save').click()

        assertThat(document.getInputElementById('name').getAttribute('class')).contains('error')
        assertThat(document.getInputElementById('amount').getAttribute('class')).contains('error')
        assertThat(document.getElementById('saved').innerText).isEqualTo('initial:3')
    }
}

@Page('/groovy/{id}.html')
@HtmlFile('GroovyPage.html')
class GroovyPage {

    private GroovyForm saved = new GroovyForm(name: 'initial', amount: 3)

    @ModelData('headline')
    String headline(@PathVariable('id') Integer id, @QueryParameter('mode') String mode) {
        "Item ${id} ${mode}"
    }

    @FormData('form')
    GroovyForm form() {
        saved
    }

    @ModelData('saved')
    String saved() {
        "${saved.name}:${saved.amount}"
    }

    @Action('save')
    void save(@FormData('form') GroovyForm form) {
        saved = form
    }
}

class GroovyForm {
    @Mandatory
    String name
    int amount
}
