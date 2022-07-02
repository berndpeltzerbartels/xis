package one.xis.micronaut


import one.xis.Widget

@Widget
class TestWidget {

    TestWidget() {
        System.out.println("Huhu !");
    }

    void test() {
        System.out.println("test")
    }
}
