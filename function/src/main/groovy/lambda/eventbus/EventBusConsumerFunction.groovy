package lambda.eventbus

import com.agorapulse.micronaut.aws.apigateway.ws.event.WebSocketRequest
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.fasterxml.jackson.databind.ObjectMapper
import groovy.transform.Field
import io.micronaut.context.event.ApplicationEventPublisher

import javax.inject.Inject

@Inject @Field ApplicationEventPublisher publisher
@Inject @Field ObjectMapper mapper

void consume(SNSEvent event) {
    event.records.each { record ->
        publisher.publishEvent(
            mapper.readValue(record.SNS.message, WebSocketRequest)
        )
    }
}
