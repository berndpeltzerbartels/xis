package test.frontlet.hierarchy;

import one.xis.context.IntegrationTestContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HierarchicalFrontletParameterTest {

    private IntegrationTestContext context;

    @BeforeEach
    void init() {
        context = IntegrationTestContext.builder()
                .withSingleton(HierarchicalFrontletParameterPage.class)
                .withSingleton(HierarchicalParentFrontlet.class)
                .withSingleton(HierarchicalChildFrontlet.class)
                .build();
    }

    @Test
    void childFrontletCanUseParentFrontletParametersAndOverrideThemLocally() {
        var client = context.openPage("/hierarchical-frontlet-parameters.html");

        assertThat(client.getDocument().getElementById("parent-project").getInnerText()).isEqualTo("42");
        assertThat(client.getDocument().getElementById("parent-view").getInnerText()).isEqualTo("parent");
        assertThat(client.getDocument().getElementById("child-project").getInnerText()).isEqualTo("42");
        assertThat(client.getDocument().getElementById("child-step").getInnerText()).isEqualTo("7");
        assertThat(client.getDocument().getElementById("child-view").getInnerText()).isEqualTo("child");
    }
}
