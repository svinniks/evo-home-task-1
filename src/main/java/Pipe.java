import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Pipe {
    char type;
    List<State> states;
    State initialState;
    List<State> allowedStates;

    void initL(int code) {
        type = 'L';

        states.add(new State(new String[]{"1", "0", "0", "1"}));
        states.add(new State(new String[]{"1", "1", "0", "0"}));
        states.add(new State(new String[]{"0", "1", "1", "0"}));
        states.add(new State(new String[]{"0", "0", "1", "1"}));

        if (code == 9499)
            initialState = states.get(0);
        else if (code == 9495)
            initialState = states.get(1);
        else if (code == 9487)
            initialState = states.get(2);
        else if (code == 9491)
            initialState = states.get(3);
    }

    void initTip(int code) {
        type = '!';

        states.add(new State(new String[]{"1", "0", "0", "0"}));
        states.add(new State(new String[]{"0", "1", "0", "0"}));
        states.add(new State(new String[]{"0", "0", "1", "0"}));
        states.add(new State(new String[]{"0", "0", "0", "1"}));

        if (code == 9593)
            initialState = states.get(0);
        else if (code == 9594)
            initialState = states.get(1);
        else if (code == 9595)
            initialState = states.get(2);
        else if (code == 9592)
            initialState = states.get(3);
    }

    void initX() {
        type = 'X';

        states.add(new State(new String[]{"1", "1", "1", "1"}));
        initialState = states.get(0);
    }

    void initI(int code) {
        type = 'I';

        states.add(new State(new String[]{"0", "1", "0", "1"}));
        states.add(new State(new String[]{"1", "0", "1", "0"}));

        if (code == 9473)
            initialState = states.get(0);
        else if (code == 9475)
            initialState = states.get(1);
    }

    void initT(int code) {
        type = 'T';

        states.add(new State(new String[]{"1", "1", "1", "0"}));
        states.add(new State(new String[]{"0", "1", "1", "1"}));
        states.add(new State(new String[]{"1", "0", "1", "1"}));
        states.add(new State(new String[]{"1", "1", "0", "1"}));

        if (code == 9507)
            initialState = states.get(0);
        else if (code == 9523)
            initialState = states.get(1);
        else if (code == 9515)
            initialState = states.get(2);
        else if (code == 9531)
            initialState = states.get(3);
    }

    Pipe(int code, boolean top, boolean right, boolean bottom, boolean left) {
        states = new ArrayList<>();

        switch (code) {
            case 9499: case 9495: case 9487: case 9491:
                initL(code);
                break;
            case 9593: case 9594: case 9595: case 9592:
                initTip(code);
                break;
            case 9547:
                initX();
                break;
            case 9473: case 9475:
                initI(code);
                break;
            case 9507: case 9523: case 9515: case 9531:
                initT(code);
                break;
            default:
                throw new RuntimeException("Unsupported pipe code " + code);
        }

        allowedStates = states.stream()
                .filter(state -> (state.getEdge(0).equals("0") || !top)
                        && (state.getEdge(1).equals("0") || !right)
                        && (state.getEdge(2).equals("0") || !bottom)
                        && (state.getEdge(3).equals("0") || !left)
                )
                .collect(Collectors.toList());
    }
}
