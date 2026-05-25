package test.page.modelcalls;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ModelCallCountTest {

    @Test
    void pageLoadCallsModelDataOnceAndActionReloadsItOnce() {
        var context = IntegrationTestContext.builder()
                .withPackage("test.page.modelcalls")
                .build();
        var client = context.openPage(ModelCallPage.class);
        var counter = context.getSingleton(ModelCallCounter.class);

        assertThat(client.getDocument().getElementById("page-calls").getInnerText()).isEqualTo("1");
        assertThat(counter.getPageModelCalls()).isEqualTo(1);
        assertThat(counter.getPageActionCalls()).isZero();

        client.getDocument().getElementById("touch").click();

        assertThat(client.getDocument().getElementById("page-calls").getInnerText()).isEqualTo("2");
        assertThat(counter.getPageModelCalls()).isEqualTo(2);
        assertThat(counter.getPageActionCalls()).isEqualTo(1);
    }

    @Test
    void pageActionReloadsFrontletModelDataOnce() {
        var context = IntegrationTestContext.builder()
                .withPackage("test.page.modelcalls")
                .build();
        var client = context.openPage(ModelCallFrontletPage.class);
        var counter = context.getSingleton(ModelCallCounter.class);

        assertThat(client.getDocument().getElementById("page-calls").getInnerText()).isEqualTo("1");
        assertThat(client.getDocument().getElementById("frontlet-calls").getInnerText()).isEqualTo("1");
        assertThat(counter.getPageModelCalls()).isEqualTo(1);
        assertThat(counter.getFrontletModelCalls()).isEqualTo(1);

        client.getDocument().getElementById("touch").click();

        assertThat(client.getDocument().getElementById("page-calls").getInnerText()).isEqualTo("2");
        assertThat(client.getDocument().getElementById("frontlet-calls").getInnerText()).isEqualTo("2");
        assertThat(counter.getPageModelCalls()).isEqualTo(2);
        assertThat(counter.getPageActionCalls()).isEqualTo(1);
        assertThat(counter.getFrontletModelCalls()).isEqualTo(2);
    }

    @Test
    void modelDataLoadControlsInitialAndAfterActionCalls() {
        var context = IntegrationTestContext.builder()
                .withPackage("test.page.modelcalls")
                .build();
        var client = context.openPage(ModelDataLifecyclePage.class);
        var page = context.getSingleton(ModelDataLifecyclePage.class);

        assertThat(client.getDocument().getElementById("always").getInnerText()).isEqualTo("always-1");
        assertThat(client.getDocument().getElementById("initial").getInnerText()).isEqualTo("initial-1");
        assertThat(client.getDocument().getElementById("after-action").getInnerText()).isEmpty();
        assertThat(client.getDocument().getElementById("selected").getInnerText()).isEqualTo("first");
        assertThat(page.getAlwaysCalls()).isEqualTo(1);
        assertThat(page.getInitialCalls()).isEqualTo(1);
        assertThat(page.getAfterActionCalls()).isZero();

        client.getDocument().getElementById("select-second").click();

        assertThat(client.getDocument().getElementById("always").getInnerText()).isEqualTo("always-2");
        assertThat(client.getDocument().getElementById("initial").getInnerText()).isEmpty();
        assertThat(client.getDocument().getElementById("after-action").getInnerText()).isEqualTo("after-action-1");
        assertThat(client.getDocument().getElementById("selected").getInnerText()).isEqualTo("second");
        assertThat(page.getAlwaysCalls()).isEqualTo(2);
        assertThat(page.getInitialCalls()).isEqualTo(1);
        assertThat(page.getAfterActionCalls()).isEqualTo(1);
    }

    @Test
    void formDataLoadControlsInitialAndAfterActionCalls() {
        var context = IntegrationTestContext.builder()
                .withPackage("test.page.modelcalls")
                .build();
        var client = context.openPage(FormDataLifecyclePage.class);
        var page = context.getSingleton(FormDataLifecyclePage.class);

        assertThat(client.getDocument().getInputElementById("value").getValue()).isEqualTo("initial-1");
        assertThat(page.getInitialCalls()).isEqualTo(1);
        assertThat(page.getAfterActionCalls()).isZero();

        client.getDocument().getElementById("save").click();

        assertThat(client.getDocument().getInputElementById("value").getValue()).isEqualTo("after-action-1");
        assertThat(page.getInitialCalls()).isEqualTo(1);
        assertThat(page.getAfterActionCalls()).isEqualTo(1);
    }

    @Test
    void frontletFormDataLoadControlsInitialAndAfterActionCalls() {
        var context = IntegrationTestContext.builder()
                .withPackage("test.page.modelcalls")
                .build();
        var client = context.openPage(FormDataLifecycleFrontletPage.class);
        var frontlet = context.getSingleton(FormDataLifecycleFrontlet.class);

        assertThat(client.getDocument().getInputElementById("frontlet-value").getValue()).isEqualTo("frontlet-initial-1");
        assertThat(frontlet.getInitialCalls()).isEqualTo(1);
        assertThat(frontlet.getAfterActionCalls()).isZero();

        client.getDocument().getElementById("frontlet-save").click();

        assertThat(client.getDocument().getInputElementById("frontlet-value").getValue()).isEqualTo("frontlet-after-action-1");
        assertThat(frontlet.getInitialCalls()).isEqualTo(1);
        assertThat(frontlet.getAfterActionCalls()).isEqualTo(1);
    }

    @Test
    void embeddedDefaultFrontletCallsInitialSharedModelData() {
        var context = IntegrationTestContext.builder()
                .withPackage("test.page.modelcalls")
                .build();
        var client = context.openPage(EmbeddedInitialSharedValuePage.class);
        var frontlet = context.getSingleton(EmbeddedInitialSharedValueFrontlet.class);

        assertThat(client.getDocument().getElementById("selected").getInnerText()).isEqualTo("root-selected");
        assertThat(client.getDocument().getElementById("title").getInnerText()).isEqualTo("title-root-selected");
        assertThat(frontlet.getRootCalls()).isEqualTo(1);
        assertThat(frontlet.getInitialSelectionCalls()).isEqualTo(1);
        assertThat(frontlet.getTitleCalls()).isEqualTo(1);
    }

    @Test
    void nestedEmbeddedDefaultFrontletCallsInitialSharedModelData() {
        var context = IntegrationTestContext.builder()
                .withPackage("test.page.modelcalls")
                .build();
        var client = context.openPage(EmbeddedInitialSharedValuePage.class);
        var frontlet = context.getSingleton(EmbeddedInitialSharedValueNestedFrontlet.class);

        assertThat(client.getDocument().getElementById("nested-selected").getInnerText()).isEqualTo("nested-root-selected");
        assertThat(client.getDocument().getElementById("nested-title").getInnerText()).isEqualTo("title-nested-root-selected");
        assertThat(frontlet.getRootCalls()).isEqualTo(1);
        assertThat(frontlet.getInitialSelectionCalls()).isEqualTo(1);
        assertThat(frontlet.getTitleCalls()).isEqualTo(1);
    }

    @Test
    void embeddedDefaultFrontletCallsInitialSharedModelDataWhenItReturnsNull() {
        var context = IntegrationTestContext.builder()
                .withPackage("test.page.modelcalls")
                .build();
        var client = context.openPage(EmbeddedInitialSharedValuePage.class);
        var frontlet = context.getSingleton(EmbeddedInitialNullSharedValueFrontlet.class);

        assertThat(client.getDocument().getElementById("null-title").getInnerText()).isEqualTo("new");
        assertThat(frontlet.getInitialSelectionCalls()).isEqualTo(1);
        assertThat(frontlet.getTitleCalls()).isEqualTo(1);
    }
}
