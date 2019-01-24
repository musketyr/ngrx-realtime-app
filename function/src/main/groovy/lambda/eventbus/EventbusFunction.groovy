package lambda.eventbus

import com.agorapulse.micronaut.aws.apigateway.ws.MessageSender
import com.agorapulse.micronaut.aws.apigateway.ws.MessageSenderFactory
import com.agorapulse.micronaut.aws.apigateway.ws.event.EventType
import com.agorapulse.micronaut.aws.apigateway.ws.event.WebSocketRequest
import com.agorapulse.micronaut.aws.apigateway.ws.event.WebSocketResponse
import com.amazonaws.AmazonClientException
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.Field

import javax.inject.Inject

@Inject @Field MessageSenderFactory factory
@Inject @Field ConnectedClientsHolder clients
@Inject @Field CounterService counterService
@Inject @Field ObjectMapper objectMapper

WebSocketResponse lambdaEventbus(WebSocketRequest event) {
    MessageSender sender = factory.create(event.requestContext)

    println event.requestContext.routeKey
    println event.body

    switch (event.requestContext.eventType) {
        case EventType.CONNECT:
            clients.registerClient(event.requestContext.connectionId)
            break
        case EventType.MESSAGE:
            Object remoteAction = objectMapper.readValue(event.body, Object)

            switch (remoteAction.type) {
                case 'publish':
                    if (remoteAction.body?.type == "[Counter] Increment") {
                        counterService.increment()
                    } else if (remoteAction.body?.type == "[Counter] Decrement") {
                        counterService.decrement()
                    } else if (remoteAction.body?.type == "[Counter] Reset") {
                        counterService.reset()
                    }

                    for (String id in clients.connectedClients) {
                        try {
                            sender.send(id, event.body)
                        } catch (AmazonClientException ignore) {
                            // already gone
                            clients.unregisterClient(id)
                        }
                    }

                    break;

                case 'send':
                    if (remoteAction.address == 'counter::total') {
                        sender.send(event.requestContext.connectionId, objectMapper.writeValueAsString(
                            address: remoteAction.replyAddress,
                            body: [total: counterService.total()]
                        ))
                    }
                    break;
            }


            break
        case EventType.DISCONNECT:
            clients.unregisterClient(event.requestContext.connectionId)
            break
    }

    return WebSocketResponse.OK
}
