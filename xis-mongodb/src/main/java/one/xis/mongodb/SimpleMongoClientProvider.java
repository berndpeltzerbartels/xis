package one.xis.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import lombok.RequiredArgsConstructor;
import one.xis.context.Bean;
import one.xis.context.Component;
import one.xis.context.Value;

@Component
@RequiredArgsConstructor
class SimpleMongoClientProvider {

    @Value(value = "xis.mongo.connection-string", mandatory = false)
    private String connectionString;

    @Bean
    MongoClient mongoClient() {
        if (connectionString == null || connectionString.isBlank()) {
            return MongoClients.create();
        }
        return MongoClients.create(connectionString);
    }
}
