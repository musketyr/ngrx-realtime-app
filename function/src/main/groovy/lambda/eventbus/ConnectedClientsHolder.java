package lambda.eventbus;

import java.util.Set;

public interface ConnectedClientsHolder {
    void registerClient(String connectionId);
    void unregisterClient(String connectionId);
    Set<String> getConnectedClients();
}
