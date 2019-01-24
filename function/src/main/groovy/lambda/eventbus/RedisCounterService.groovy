package lambda.eventbus

import groovy.transform.CompileStatic
import io.lettuce.core.api.StatefulRedisConnection
import io.micronaut.context.annotation.Requires
import io.micronaut.context.annotation.Value

import javax.inject.Singleton

@Singleton
@CompileStatic
@Requires(beans = StatefulRedisConnection)
class RedisCounterService implements CounterService {

    private final StatefulRedisConnection<String, String> connection
    private final String counterKey

    RedisCounterService(
        @Value('${counter.key:WSTEST_COUNTER}') String counterKey,
        StatefulRedisConnection<String, String> connection
    ) {
        this.counterKey = counterKey
        this.connection = connection
    }

    @Override
    int decrement() {
        return connection.sync().decr(counterKey)
    }

    @Override
    int increment() {
        return connection.sync().incr(counterKey)
    }

    @Override
    void reset() {
        connection.sync().set(counterKey, '0')
    }

    @Override
    int total() {
        return Integer.valueOf(connection.sync().get(counterKey), 10)
    }
}
