package one.xis.context;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class InitMethodInvokerTest {


    @Test
    void invokeAncestorMethods() throws NoSuchMethodException {
        var invoker = new InitMethodInvoker(A.class.getDeclaredMethod("init"));
        invoker.onComponentCreated(new A());
        invoker.onComponentCreated(new B());
        invoker.onComponentCreated(new C());

        assertThat(invoker.getOwners()).hasSize(3);
    }

    class A {
        void init() {
        }
    }

    class B extends A {

    }

    class C extends A {

    }

}