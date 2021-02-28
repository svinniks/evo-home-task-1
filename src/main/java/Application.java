import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Set;

import static java.nio.charset.StandardCharsets.UTF_8;

public class Application {
    public static void main(String[] args) throws Exception {
        try (var inputStream = Application.class.getResourceAsStream("map.txt")) {
            var lines = new String(inputStream.readAllBytes(), UTF_8).split("\\r\\n");
            var map = new PipeMap(lines);

            var states = new ArrayList<>(map.computeStates());

            try (var os = new FileOutputStream("rotations.txt"); var pw = new PrintWriter(os)) {
                states.get(0).rotate(map.pipes, 0, 0, pw);
            }
        }
//        PipesWebSocketClient c = new PipesWebSocketClient(new URI("wss://hometask.eg1236.com/game-pipes/"));
//        c.connect();
    }
}
