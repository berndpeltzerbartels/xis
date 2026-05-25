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
}
