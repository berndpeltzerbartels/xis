package one.xis.context;

import org.junit.jupiter.api.Test;

class InitMethodWrapperInvokerTest {

    // TODO
    @Test
    void invokeAncestorMethods() throws NoSuchMethodException {
        /*
        var invoker = new MethodWrapper(A.class.getDeclaredMethod("init"));
        invoker.onComponentCreated(new A());
        invoker.onComponentCreated(new B());
        invoker.onComponentCreated(new C());
*/
        //  assertThat(invoker.getOwners()).hasSize(3);
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