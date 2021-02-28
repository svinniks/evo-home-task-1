import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Set;

public class PipesWebSocketClient extends WebSocketClient {
    public PipesWebSocketClient(URI serverUri) {
        super(serverUri);
    }

    @Override
    public void onOpen(ServerHandshake serverHandshake) {
        send("new 1");
    }

    @Override
    public void onMessage(String s) {
        if (s.startsWith("new:"))
            send("map");
        else if (s.startsWith("map:")) {
            var lines = s.substring(5).split("\\n");
            var map = new PipeMap(lines);
            Set<State> states = null;
            try {
                states = map.computeStates();
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (states.size() == 0) {
                System.out.println(s);
                this.close();
            } else
                send("new 1");
        }
    }

    @Override
    public void onClose(int i, String s, boolean b) {

    }

    @Override
    public void onError(Exception e) {

    }
}
