package lambda.eventbus

import com.agorapulse.micronaut.aws.apigateway.ws.MessageSender
import com.agorapulse.micronaut.aws.apigateway.ws.MessageSenderFactory
import com.agorapulse.micronaut.aws.apigateway.ws.event.EventType
import com.agorapulse.micronaut.aws.apigateway.ws.event.WebSocketRequest
import com.amazonaws.AmazonClientException
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileStatic
import io.lettuce.core.api.StatefulRedisConnection
import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import io.micronaut.context.event.ApplicationEventListener

import javax.inject.Singleton

@Singleton
@CompileStatic
@Requires(beans = StatefulRedisConnection)
class ConnectionManager implements ApplicationEventListener<WebSocketRequest> {

    private final StatefulRedisConnection<String, String> connection
    private final String connectedClientKey
    private final MessageSenderFactory factory
    private final ObjectMapper objectMapper

    ConnectionManager(
        @Value('${connected.clients.key:WSTEST_CONNECTED_CLIENTS}') String connectedClientKey,
        StatefulRedisConnection<String, String> connection,
        MessageSenderFactory factory,
        ObjectMapper objectMapper
    ) {
        this.connectedClientKey = connectedClientKey
        this.connection = connection
        this.factory = factory
        this.objectMapper = objectMapper
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

    @Override
    void onApplicationEvent(WebSocketRequest event) {
        switch (event.requestContext.eventType) {
            case EventType.CONNECT:
                registerClient(event.requestContext.connectionId)
                break
            case EventType.MESSAGE:
                Object remoteAction = objectMapper.readValue(event.body, Object)
                if (remoteAction['type'] == 'publish') {
                    MessageSender sender = factory.create(event.requestContext)
                    for (String id in connectedClients) {
                        try {
                            sender.send(id, event.body)
                        } catch (AmazonClientException ignore) {
                            // already gone
                            unregisterClient(id)
                        }
                    }
                }
                break
            case EventType.DISCONNECT:
                unregisterClient(event.requestContext.connectionId)
                break
        }
    }
}
