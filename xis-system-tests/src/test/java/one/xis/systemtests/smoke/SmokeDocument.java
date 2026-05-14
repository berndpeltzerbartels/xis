package one.xis.systemtests.smoke;

import one.xis.mongodb.MongoDocument;

@MongoDocument("smoke")
class SmokeDocument {
    String id;
    String value;

    SmokeDocument() {
    }

    SmokeDocument(String id, String value) {
        this.id = id;
        this.value = value;
    }
}
