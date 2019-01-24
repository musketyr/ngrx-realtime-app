package lambda.eventbus

import groovy.transform.CompileStatic
import io.lettuce.core.api.StatefulRedisConnection
import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value

import javax.inject.Singleton

@Singleton
@CompileStatic
@Requires(beans = StatefulRedisConnection)
class RedisConnectedClientsHolder implements ConnectedClientsHolder {

    private final StatefulRedisConnection<String, String> connection
    private final String connectedClientKey

    RedisConnectedClientsHolder(
            @Value('${connected.clients.key:WSTEST_CONNECTED_CLIENTS}') String connectedClientKey,
            StatefulRedisConnection<String, String> connection
    ) {
        this.connectedClientKey = connectedClientKey
        this.connection = connection
    }

    void registerClient(String connectionId) {
        connection.sync().rpush(connectedClientKey, connectionId)
    }

    void unregisterClient(String connectionId) {
        connection.sync().lrem(connectedClientKey, 0, connectionId)
    }

    Set<String> getConnectedClients() {
        connection.sync().lrange(connectedClientKey, 0, 1000) as Set<String>
    }
}
