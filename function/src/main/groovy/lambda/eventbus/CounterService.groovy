package lambda.eventbus

import com.agorapulse.micronaut.aws.apigateway.ws.MessageSender
import com.agorapulse.micronaut.aws.apigateway.ws.MessageSenderFactory
import com.agorapulse.micronaut.aws.apigateway.ws.event.EventType
import com.agorapulse.micronaut.aws.apigateway.ws.event.WebSocketRequest
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.CompileDynamic
import groovy.transform.CompileStatic
import io.lettuce.core.api.StatefulRedisConnection
import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value
import io.micronaut.context.event.ApplicationEventListener

import javax.inject.Singleton

@Singleton
@CompileStatic
@Requires(beans = StatefulRedisConnection)
class CounterService implements ApplicationEventListener<WebSocketRequest> {

    private final StatefulRedisConnection<String, String> connection
    private final String counterKey
    private final MessageSenderFactory factory
    private final ObjectMapper objectMapper

    CounterService(
        @Value('${counter.key:WSTEST_COUNTER}') String counterKey,
        StatefulRedisConnection<String, String> connection,
        MessageSenderFactory factory,
        ObjectMapper objectMapper
    ) {
        this.counterKey = counterKey
        this.connection = connection
        this.factory = factory
        this.objectMapper = objectMapper
    }

    int decrement() {
        return connection.sync().decr(counterKey)
    }

    int increment() {
        return connection.sync().incr(counterKey)
    }

    void reset() {
        connection.sync().set(counterKey, '0')
    }

    int total() {
        return Integer.valueOf(connection.sync().get(counterKey), 10)
    }

    @Override
    @CompileDynamic
    void onApplicationEvent(WebSocketRequest event) {
        if (event.requestContext.eventType == EventType.MESSAGE) {
            Object remoteAction = objectMapper.readValue(event.body, Object)

            if (remoteAction.type == 'publish') {
                handlePublish(remoteAction)
            } else if (remoteAction.type == 'send') {
                handleSend(remoteAction, event)
            }
        }
    }

    @CompileDynamic
    private void handleSend(Object remoteAction, WebSocketRequest event) {
        if (remoteAction.address == 'counter::total') {
            MessageSender sender = factory.create(event.requestContext)
            sender.send(event.requestContext.connectionId, objectMapper.writeValueAsString(
                address: remoteAction.replyAddress,
                body: [total: total()]
            ))
        }
    }

    @CompileDynamic
    private void handlePublish(Object remoteAction) {
        if (remoteAction.body?.type == "[Counter] Increment") {
            increment()
        } else if (remoteAction.body?.type == "[Counter] Decrement") {
            decrement()
        } else if (remoteAction.body?.type == "[Counter] Reset") {
            reset()
        }
    }
}
