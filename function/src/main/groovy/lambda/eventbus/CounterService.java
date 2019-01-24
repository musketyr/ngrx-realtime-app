package lambda.eventbus;

public interface CounterService {

    int decrement();
    int increment();
    void reset();
    int total();

}
