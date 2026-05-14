package one.xis.systemtests.smoke;

import one.xis.mongodb.MongoCrudRepository;

@MongoSmokeRepositoryProxy
interface MongoSmokeRepository extends MongoCrudRepository<SmokeDocument, String> {
}
