package lambda.eventbus

import com.agorapulse.micronaut.aws.apigateway.ws.event.WebSocketRequest
import com.agorapulse.micronaut.aws.apigateway.ws.event.WebSocketResponse
import groovy.transform.Field

import javax.inject.Inject

@Inject @Field WebSocketRequestPublisher publisher

WebSocketResponse produce(WebSocketRequest event) {
    publisher.publishEvent(event)
    return WebSocketResponse.OK
}
