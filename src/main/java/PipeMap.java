import java.util.Set;

public class PipeMap {
    int w;
    int h;
    Pipe[][] pipes;

    PipeMap(String[] lines) {
        h = lines.length;
        w = lines[1].length();
        pipes = new Pipe[h][w];

        for (var y = 0; y < h; y++) {
            for (var x = 0; x < w; x++) {
                var pipe = new Pipe(
                        lines[y].codePointAt(x),
                        y == 0,
                        x == w - 1,
                        y == h - 1,
                        x == 0
                );

                pipes[y][x] = pipe;
                //System.out.print(pipe.type);
            }

            //System.out.println();
        }
    }

    Set<State> computeStates() throws Exception {
        var region = new Region(null, null, this, 1, 0, 0, w, h);
        region.computeEdgesSignatures();
        region.computeStates();

        return region.states;
    }
}
