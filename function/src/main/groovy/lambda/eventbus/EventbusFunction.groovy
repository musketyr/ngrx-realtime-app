package lambda.eventbus

import com.agorapulse.micronaut.aws.apigateway.ws.event.WebSocketRequest
import com.agorapulse.micronaut.aws.apigateway.ws.event.WebSocketResponse
import groovy.transform.Field
import io.micronaut.context.event.ApplicationEventPublisher

import javax.inject.Inject

@Inject @Field ApplicationEventPublisher publisher

WebSocketResponse lambdaEventbus(WebSocketRequest event) {
    publisher.publishEvent(event)
    return WebSocketResponse.OK
}
