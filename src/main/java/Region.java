import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class Region {
    class ConnectionEdgeSignature {
        Set<State> states1;
        Set<State> states2;

        public ConnectionEdgeSignature(Set<State> states1, Set<State> states2) {
            if (states1 == null || states2 == null)
                System.out.println();

            this.states1 = states1;
            this.states2 = states2;
        }
    }

    static int ID = 1;

    int id = ID++;
    Region parent;
    Integer pos;
    PipeMap map;
    int level;
    int x;
    int y;
    int w;
    int h;

    int connectionEdge;

    Direction direction;
    Region child1;
    Region child2;

    Set<State> states;
    boolean validated;
    Map<String, List<ConnectionEdgeSignature>> connectionEdges;

    Region(Region parent, Integer pos, PipeMap map, int level, int x, int y, int w, int h) {
        this.parent = parent;
        this.pos = pos;
        this.map = map;
        this.level = level;
        this.x = x;
        this.y = y;
        this.w = w;
        this.h = h;

        if (parent != null)
            connectionEdge = getConnectionEdge();

        states = new HashSet<>();
    }

    int getConnectionEdge() {
        if (parent.direction == Direction.HORIZONTAL)
            return pos == 1 ? 1 : 3;
        else
            return pos == 1 ? 2 : 0;
    }

    void split() {
        direction = w >= h ? Direction.HORIZONTAL : Direction.VERTICAL;

        if (direction == Direction.HORIZONTAL) {
            child1 = new Region(this, 1, map, level + 1, x, y, w / 2, h);
            child2 = new Region(this, 2, map, level + 1, x + w / 2, y, w - w / 2, h);
        } else {
            child1 = new Region(this, 1, map, level + 1, x, y, w, h / 2);
            child2 = new Region(this, 2, map, level + 1, x, y + h / 2, w, h - h / 2);
        }
    }

    Map<String, Set<State>> buildStateMap(Region child) {
        var stateMap = new HashMap<String, Set<State>>();

        child.states.forEach(state -> {
            var edge = state.getEdge(child.connectionEdge);

            if (!stateMap.containsKey(edge))
                stateMap.put(edge, new HashSet<>(1));

            stateMap.get(edge).add(state);
        });

        return stateMap;
    }

    void addSimpleConnectionEdgeSignatures(Set<State> states1, Set<State> states2, int edge) {
        var signatures = new TreeMap<String, Set<State>>();

        if (edge == 0 || edge == 3)
            states1.forEach(state -> {
                var edgeSignature = state.getEdge(edge);

                if (!signatures.containsKey(edgeSignature))
                    signatures.put(edgeSignature, new TreeSet<>());

                signatures.get(edgeSignature).add(state);
            });
        else
            states2.forEach(state -> {
                var edgeSignature = state.getEdge(edge);

                if (!signatures.containsKey(edgeSignature))
                    signatures.put(edgeSignature, new TreeSet<>());

                signatures.get(edgeSignature).add(state);
            });

        for (var signature : signatures.keySet()) {
            if (!connectionEdges.containsKey(signature))
                connectionEdges.put(signature, new ArrayList<>());

            connectionEdges.get(signature).add(
                    edge == 0 || edge == 3
                            ? new ConnectionEdgeSignature(signatures.get(signature), states2)
                            : new ConnectionEdgeSignature(states1, signatures.get(signature))
            );
        }
    }

    void addCombinedConnectionEdgeSignatures(Set<State> states1, Set<State> states2, int edge) {
        var signatures1 = new TreeMap<String, Set<State>>();

        states1.forEach(state -> {
            var edgeSignature = state.getEdge(edge);

            if (!signatures1.containsKey(edgeSignature))
                signatures1.put(edgeSignature, new TreeSet<>());

            signatures1.get(edgeSignature).add(state);
        });

        var signatures2 = new TreeMap<String, Set<State>>();

        states2.forEach(state -> {
            var edgeSignature = state.getEdge(edge);

            if (!signatures2.containsKey(edgeSignature))
                signatures2.put(edgeSignature, new TreeSet<>());

            signatures2.get(edgeSignature).add(state);
        });

        for (var signature1 : signatures1.keySet())
            for (var signature2 : signatures2.keySet()) {
                var signature = signature1 + signature2;

                if (!connectionEdges.containsKey(signature))
                    connectionEdges.put(signature, new ArrayList<>());

                connectionEdges.get(signature).add(
                        new ConnectionEdgeSignature(signatures1.get(signature1), signatures2.get(signature2))
                );
            }
    }

    void computeEdgesSignatures() throws Exception {
        connectionEdges = new TreeMap<>();

        if (w * h == 1) {
            var pipe = map.pipes[y][x];
            pipe.states.forEach(state -> connectionEdges.put(state.getEdge(connectionEdge), null));
        } else {
            split();

            child1.computeEdgesSignatures();
            child2.computeEdgesSignatures();

            var intersectionEdges = new HashSet<>(child1.connectionEdges.keySet());
            intersectionEdges.retainAll(child2.connectionEdges.keySet());

            child1.connectionEdges.keySet().retainAll(intersectionEdges);
            child2.connectionEdges.keySet().retainAll(intersectionEdges);

            child1.computeStates();
            child2.computeStates();

            var stateMap1 = buildStateMap(child1);
            var stateMap2 = buildStateMap(child2);

            intersectionEdges = new HashSet<>(stateMap1.keySet());
            intersectionEdges.retainAll(stateMap2.keySet());

            if (level <= 8)
                System.out.println("Computing...");

            connectionEdges = new TreeMap<>();

            for (var edge : intersectionEdges)
                if (direction == Direction.HORIZONTAL)
                    if (connectionEdge == 3)
                        addSimpleConnectionEdgeSignatures(stateMap1.get(edge), stateMap2.get(edge), 3);
                    else if (connectionEdge == 1)
                        addSimpleConnectionEdgeSignatures(stateMap1.get(edge), stateMap2.get(edge), 1);
                    else if (connectionEdge == 0)
                        addCombinedConnectionEdgeSignatures(stateMap1.get(edge), stateMap2.get(edge), 0);
                    else
                        addCombinedConnectionEdgeSignatures(stateMap1.get(edge), stateMap2.get(edge), 2);
                else if (connectionEdge == 0)
                    addSimpleConnectionEdgeSignatures(stateMap1.get(edge), stateMap2.get(edge), 0);
                else if (connectionEdge == 2)
                    addSimpleConnectionEdgeSignatures(stateMap1.get(edge), stateMap2.get(edge), 2);
                else if (connectionEdge == 1)
                    addCombinedConnectionEdgeSignatures(stateMap1.get(edge), stateMap2.get(edge), 1);
                else
                    addCombinedConnectionEdgeSignatures(stateMap1.get(edge), stateMap2.get(edge), 3);

            if (level <= 8)
                System.out.println("Copmuted (" + connectionEdges.keySet().size() + ")");
        }
    }

    void computeStates() throws IOException {
        if (w * h == 1) {
            var pipe = map.pipes[y][x];

            states = pipe.allowedStates
                    .stream()
                    .filter(state -> connectionEdges.containsKey(state.getEdge(connectionEdge)))
                    .collect(Collectors.toSet());

            states.forEach(state -> state.validate());
        } else {
            var buffer = new HashSet<State>();

            if (level <= 8)
                System.out.println("Generating...");

            long a = 0;
            long b = 0;
            long total = 0;

            for (var edgeSignature : connectionEdges.keySet())
                for (var edge : connectionEdges.get(edgeSignature))
                    total += edge.states1.size() * edge.states2.size();


            for (var edges : connectionEdges.values())
                for (var edge : edges)
                    for (var state1 : edge.states1)
                        for (var state2: edge.states2) {
                            var newState = new State(w, h, direction, state1, state2);
                            buffer.add(newState);

                            if (buffer.size() == 100000) {
                                if (level <= 8)
                                    System.out.println("Validating " + b + " of " + total + "...");

                                buffer.parallelStream().forEach(state -> state.validate());

                                for (var state : buffer) {
                                    if (!state.cycle && (!state.closed || level == 1)) {
                                        states.add(state);
                                        b++;
                                    }
                                };

                                buffer.clear();
                            }
                        }

            if (level <= 8)
                System.out.println("Validating " + b + " of " + total + "...");

            buffer.parallelStream().forEach(state -> state.validate());

            for (var state : buffer) {
                if (!state.cycle && (!state.closed || level == 1)) {
                    states.add(state);
                    b++;
                }
            };

            states.forEach(State::reset);

            child1.connectionEdges = null;
            child1.states = null;

            child1.connectionEdges = null;
            child2.states = null;

            if (level <= 8) {
                System.out.println(level + " (" + states.size() + ")");
            }
        }
    }
}
