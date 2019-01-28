package lambda.eventbus

import com.agorapulse.micronaut.aws.apigateway.ws.event.WebSocketRequest
import com.agorapulse.micronaut.aws.sns.annotation.NotificationClient
import groovy.transform.CompileStatic

@CompileStatic
@NotificationClient
interface WebSocketRequestPublisher {

    void publishEvent(WebSocketRequest request)

}
