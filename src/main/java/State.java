import java.io.PrintWriter;
import java.util.*;

public class State implements Comparable<State> {
    final static Map<String, Character> PIPES;

    static {
        PIPES = new HashMap<>();

        PIPES.put("1000", '╹');
        PIPES.put("0100", '╺');
        PIPES.put("0010", '╻');
        PIPES.put("0001", '╸');

        PIPES.put("1110", '┣');
        PIPES.put("0111", '┳');
        PIPES.put("1011", '┫');
        PIPES.put("1101", '┻');

        PIPES.put("1100", '┗');
        PIPES.put("0110", '┏');
        PIPES.put("0011", '┓');
        PIPES.put("1001", '┛');

        PIPES.put("1010", '┃');
        PIPES.put("0101", '━');

        PIPES.put("1111", '╋');
    }

    @Override
    public int compareTo(State o) {
        return id - o.id;
    }

    class Edge {
        Node node1;
        Node node2;

        public Edge(Node node1, Node node2) {
            this.node1 = node1;
            this.node2 = node2;
        }

        Node getTarget(Node node) {
            return node1 == node ? node2 : node1;
        }
    }

    class Node {
        short star;
        boolean visited;
        Collection<Edge> edges;

        Node(short star) {
            this.star = star;
            edges = new ArrayList<>(2);
        }

        boolean visit(Edge incomingEdge, short[] starMap, short starId) {
            if (visited)
                return true;

            visited = true;
            starMap[star - 1] = starId;

            for (Edge edge : edges)
                if (edge != incomingEdge)
                    if (edge.getTarget(this).visit(edge, starMap, starId))
                        return true;

            return false;
        }
    }

    static int ID = 1;

    int id = ID++;
    String signature;
    private String[] edges;
    short[][] connections;

    int w;
    int h;
    Direction direction;
    State child1;
    State child2;

    boolean cycle;
    boolean closed;

    public State(int w, int h, Direction direction, State child1, State child2) {
        this.w = w;
        this.h = h;
        this.direction = direction;
        this.child1 = child1;
        this.child2 = child2;
    }

    public State(String[] edges) {
        w = 1;
        h = 1;
        this.edges = edges;
        signature = String.join("", edges);
    }

    void visitEdge(StringBuilder builder, int edge) {
        if (edges != null)
            builder.append(edges[edge]);
        else if (direction == Direction.HORIZONTAL)
            if (edge == 0) {
                child1.visitEdge(builder, 0);
                child2.visitEdge(builder, 0);
            } else if (edge == 1)
                child2.visitEdge(builder, 1);
            else if (edge == 2) {
                child1.visitEdge(builder, 2);
                child2.visitEdge(builder, 2);
            } else
                child1.visitEdge(builder, 3);
        else if (edge == 0)
            child1.visitEdge(builder, 0);
        else if (edge == 1) {
            child1.visitEdge(builder, 1);
            child2.visitEdge(builder, 1);
        } else if (edge == 2)
            child2.visitEdge(builder, 2);
        else {
            child1.visitEdge(builder, 3);
            child2.visitEdge(builder, 3);
        }
    }

    String getEdge(int edge) {
        var builder = new StringBuilder();
        visitEdge(builder, edge);
        return builder.toString();
    }

    boolean isLeaf() {
        return child1 == null;
    }

    String[] combineEdges(String[] edges1, String[] edges2) {
        if (direction == Direction.HORIZONTAL)
            return new String[] {
                    edges1[0] + edges2[0],
                    edges2[1],
                    edges1[2] + edges2[2],
                    edges1[3]
            };
        else
            return new String[] {
                    edges1[0],
                    edges1[1] + edges2[1],
                    edges2[2],
                    edges1[3] + edges2[3]
            };
    }

    void validate() {
        if (isLeaf())
            connections = new short[][] {
                    {(short) (edges[0] == "0" ? 0 : 1)},
                    {(short) (edges[1] == "0" ? 0 : 1)},
                    {(short) (edges[2] == "0" ? 0 : 1)},
                    {(short) (edges[3] == "0" ? 0 : 1)}
            };
        else
            combine();
    }

    void combine() {

        int connectionEdge1 = direction == Direction.HORIZONTAL ? 1 : 2;
        int connectionEdge2 = direction == Direction.HORIZONTAL ? 3 : 0;

        var connection1 = child1.connections[connectionEdge1];
        var connection2 = child2.connections[connectionEdge2];

        short starId = 0;

        for (var edge = 0; edge < 4; edge++)
            for (var star : child1.connections[edge])
                if (star > 0)
                    starId = (short) Math.max(starId, star);

        var offset = starId;

        for (var edge = 0; edge < 4; edge++)
            for (var star : child2.connections[edge])
                if (star > 0)
                    starId = (short) Math.max(starId, star + offset);

        var nodes = new HashMap<Short, Node>();

        for (var star : connection1)
            if (star > 0 && !nodes.containsKey(star))
                nodes.put(star, new Node(star));

        for (var star : connection2)
            if (star > 0 && !nodes.containsKey((short) (star + offset)))
                nodes.put((short) (star + offset), new Node((short) (star + offset)));

        for (var i = 0; i < connection1.length; i++) {
            if (connection1[i] > 0) {
                var edge = new Edge(nodes.get(connection1[i]), nodes.get((short) (connection2[i] + offset)));

                nodes.get(connection1[i]).edges.add(edge);
                nodes.get((short)(connection2[i] + offset)).edges.add(edge);
            }
        }

        var mappedStars = new ArrayList<Short>(1);
        var starMap = new short[starId];

        for (var node : nodes.values())
            if (!node.visited) {
                starId++;
                mappedStars.add(starId);

                if (node.visit(null, starMap, starId)) {
                    cycle = true;
                    return;
                }
            }

        connections = new short[][]{
                new short[w],
                new short[h],
                new short[w],
                new short[h]
        };

        var starIds = new short[starId];
        starId = 0;

        starId = fillConnection(child1.connections[0], connections[0], 0, starMap, starIds, starId, 0);
        starId = fillConnection(child1.connections[3], connections[3], 0, starMap, starIds, starId, 0);

        if (direction == Direction.HORIZONTAL) {
            var connectionOffset = child1.w;

            starId = fillConnection(child1.connections[2], connections[2], 0, starMap, starIds, starId, 0);
            starId = fillConnection(child2.connections[0], connections[0], connectionOffset, starMap, starIds, starId, offset);
            starId = fillConnection(child2.connections[2], connections[2], connectionOffset, starMap, starIds, starId, offset);
            fillConnection(child2.connections[1], connections[1], 0, starMap, starIds, starId, offset);
        } else {
            var connectionOffset = child1.h;

            starId = fillConnection(child1.connections[1], connections[1], 0, starMap, starIds, starId, 0);
            starId = fillConnection(child2.connections[3], connections[3], connectionOffset, starMap, starIds, starId, offset);
            starId = fillConnection(child2.connections[1], connections[1], connectionOffset, starMap, starIds, starId, offset);
            fillConnection(child2.connections[2], connections[2], 0, starMap, starIds, starId, offset);
        }

        for (var star : mappedStars)
            if (starIds[star - 1] == 0) {
                closed = true;
                return;
            }

        this.edges = combineEdges(child1.edges, child2.edges);
    }

    void visit(String[][] signatures, int x, int y) {
        if (w == 1 && h == 1)
            signatures[y][x] = signature;
        else if (direction == Direction.HORIZONTAL) {
            child1.visit(signatures, x, y);
            child2.visit(signatures, x + child1.edges[0].length(), y);
        } else {
            child1.visit(signatures, x, y);
            child2.visit(signatures, x, y + child1.edges[1].length());
        }
    }

    void print() {
        System.out.println(id + ": --------------------------------------");
        String[][] signatures = new String[h][w];
        visit(signatures,0, 0);

        for (var line : signatures) {
            for (var signature : line)
                System.out.print(PIPES.get(signature) + " ");

            System.out.println();
        }
    }

    void rotate(Pipe[][] pipes, int x, int y, PrintWriter pw) {
        if (w * h == 1) {
            var pipe = pipes[y][x];
            var stateI = pipe.states.indexOf(pipe.initialState);

            while (pipe.states.get(stateI) != this) {
                pw.println("rotate " + x + " " + y);
                stateI = (stateI + 1) % pipe.states.size();
            }
        } else if (direction == Direction.HORIZONTAL) {
            child1.rotate(pipes, x, y, pw);
            child2.rotate(pipes, x + child1.w, y, pw);
        } else {
            child1.rotate(pipes, x, y, pw);
            child2.rotate(pipes, x, y + child1.h, pw);
        }
    }

    short fillConnection(short[] sourceConnection, short[] targetConnection, int connectionOffset, short[] starMap, short[] starIds, short starId, int starOffset) {
        for (var i = 0; i < sourceConnection.length; i++) {
            var star = sourceConnection[i];

            if (star != 0) {
                star += starOffset;
                //star = (short) Math.max(star, starMap[star - 1]);
                star = star <= starMap.length && starMap[star - 1] != 0 ? starMap[star - 1] : star;

                if (starIds[star - 1] == 0)
                    starIds[star - 1] = ++starId;

                targetConnection[i + connectionOffset] = starIds[star - 1];
            }
        }

        return starId;
    }

    void reset() {
        if (w * h > 1) {
            if (child1.w * child1.h > 1) {
                child1.edges = null;
                child1.connections = null;
            }

            if (child2.w * child2.h > 1) {
                child2.edges = null;
                child2.connections = null;
            }
        }
    }
}

