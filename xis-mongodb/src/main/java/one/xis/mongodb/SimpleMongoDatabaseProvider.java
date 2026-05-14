package one.xis.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoDatabase;
import lombok.RequiredArgsConstructor;
import one.xis.context.Bean;
import one.xis.context.Component;
import one.xis.context.Value;

@Component
@RequiredArgsConstructor
class SimpleMongoDatabaseProvider {
    private final MongoClient mongoClient;

    @Value("xis.mongo.database")
    private String databaseName;

    @Bean
    MongoDatabase mongoDatabase() {
        return mongoClient.getDatabase(databaseName);
    }
}
