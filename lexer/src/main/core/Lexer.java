package main.core;

import main.exception.FAException;
import main.exception.InvalidFileFormatException;
import main.core.fa.DFA;
import main.util.IOHelper;
import main.core.Regex;
import main.exception.RegexException;
import main.exception.InvalidFormatException;

import java.io.*;
import java.util.*;

public class Lexer {

    private class State {
        int dfaIndex;
        int dfaState;

        State(int dfaIndex, int dfaState) {
            this.dfaIndex = dfaIndex;
            this.dfaState = dfaState;
        }
    }

    private class StateSet implements Iterable<State> {
        Set<State> states;

        StateSet() {
            states = new HashSet<>();
        }

        StateSet(Collection<State> collection) {
            states = new HashSet<>(collection);
        }

        void add(State s) {
            states.add(s);
        }

        boolean isEmpty() {
            return states.isEmpty();
        }

        @Override
        public Iterator<State> iterator() {
            return states.iterator();
        }
    }

    private static final String DELIMITER = "%%";

    private List<DFA> optimizedDFAs;

    public Lexer(String inputFilePath) throws IOException {
        if (!inputFilePath.endsWith(".l")) {
            throw new InvalidFileFormatException("Expected: .l, Actual: " + inputFilePath.substring(inputFilePath.lastIndexOf('.')));
        } else {
            File inputFile = new File(inputFilePath);
            if (!inputFile.exists()) {
                throw new FileNotFoundException(inputFilePath);
            } else {
                this.optimizedDFAs = new ArrayList<>();
                try {
                    init(new FileInputStream(inputFile));
                } catch (RegexException | InvalidFormatException | FAException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    private void init(InputStream is) throws IOException, RegexException, InvalidFormatException, FAException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        int lineNumber = 0;
        int part = 0;
        String line;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            if (!"".equals(line.trim())) {
                if (line.trim().startsWith("#")) {
                    // 注释
                    continue;
                }
                if (line.startsWith(DELIMITER) && DELIMITER.equals(line.trim())) {
                    part = 1;
                }
                if (line.equals(trimLeading(line))) {
                    if (part == 0) {
                        // 处理定义部分
                        Regex regex;
                        try {
                            regex = Regex.parse(line);
                        } catch (RegexException ex) {
                            throw new RegexException(ex.getMessage() + ", Line: " + lineNumber);
                        }
                        DFA dfa = new DFA(regex);
                        optimizedDFAs.add(dfa);
                    } else {
                        // 处理规则部分
                        // 第一个空白符后面的全部被忽略
                        // String pattern = line.split("\\s+")[0];
                    }
                } else {
                    throw new InvalidFormatException("Invalid Format: Line Starts With White Space, Line: " + lineNumber);
                }
            }
        }
    }

    public void analyze(InputStream src) throws IOException {
        File outputFile = new File("sample/tokens.out");
        if (!outputFile.exists()) {
            try {
                if (!outputFile.createNewFile()) {
                    System.err.println("Something wrong while creating the output file. ");
                    return;
                }
            } catch (IOException e) {
                System.err.println("Something wrong while creating the output file. ");
                return;
            }
        } else {
            clear(outputFile);
        }
        try {
            analyze(src, new FileOutputStream(outputFile));
        } catch (FileNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public void analyze(InputStream src, OutputStream dest) throws IOException {
        IOHelper helper = new IOHelper(src, dest);

        char[] block;
        while ((block = helper.getBlock().toCharArray()).length > 0) {

            StateSet stateSet = new StateSet();
            Queue<State> currentLayerStates = new LinkedList<>();
            for (int i = 0; i < optimizedDFAs.size(); i++) {
                State s = new State(i, 0);
                stateSet.add(s);
                currentLayerStates.offer(s);
            }
            Queue<State> nextLayerStates = new LinkedList<>();
            Stack<StateSet> stateSetStack = new Stack<>();
            stateSetStack.push(stateSet);
            stateSet = new StateSet();
            StringBuilder lexemeBuilder = new StringBuilder();

            int forward = 0;
            while (forward < block.length) {
                char ch = block[forward];
                while (!currentLayerStates.isEmpty()) {
                    State state = currentLayerStates.poll();
                    DFA dfa = optimizedDFAs.get(state.dfaIndex);
                    int toState = dfa.move(state.dfaState, ch);
                    if (toState >= 0) {
                        State s = new State(state.dfaIndex, toState);
                        nextLayerStates.offer(s);
                        stateSet.add(s);
                    }
                }
                if (!stateSet.isEmpty()) {
                    stateSetStack.push(stateSet);
                }
                if (nextLayerStates.isEmpty()) {
                    // 此时可以开始回溯
                    int dfaNo = Integer.MAX_VALUE;
                    while (!stateSetStack.isEmpty()) {
                        StateSet set = stateSetStack.pop();
                        for (State s : set) {
                            DFA dfa = optimizedDFAs.get(s.dfaIndex);
                            int state = s.dfaState;
                            if (dfa.isAcceptState(state)) {
                                if (s.dfaIndex < dfaNo) {
                                    dfaNo = s.dfaIndex;
                                }
                            }
                        }
                        forward--;
                        if (dfaNo != Integer.MAX_VALUE) {
                            break;
                        }
                    }
                    DFA dfa = optimizedDFAs.get(dfaNo);
                    helper.write(dfa.getName(), lexemeBuilder.toString());

                    stateSet = new StateSet();
                    currentLayerStates = new LinkedList<>();
                    for (int i = 0; i < optimizedDFAs.size(); i++) {
                        State s = new State(i, 0);
                        stateSet.add(s);
                        currentLayerStates.offer(s);
                    }
                    nextLayerStates = new LinkedList<>();
                    stateSetStack = new Stack<>();
                    stateSetStack.push(stateSet);
                    stateSet = new StateSet();
                    lexemeBuilder = new StringBuilder();
                } else {
                    stateSet = new StateSet();
                    currentLayerStates = nextLayerStates;
                    nextLayerStates = new LinkedList<>();
                    lexemeBuilder.append(ch);
                }
                forward++;
            }
            int dfaNo = Integer.MAX_VALUE;
            while (!stateSetStack.isEmpty()) {
                StateSet set = stateSetStack.pop();
                for (State s : set) {
                    DFA dfa = optimizedDFAs.get(s.dfaIndex);
                    int state = s.dfaState;
                    if (dfa.isAcceptState(state)) {
                        if (s.dfaIndex < dfaNo) {
                            dfaNo = s.dfaIndex;
                        }
                    }
                }
                forward--;
                if (dfaNo != Integer.MAX_VALUE) {
                    break;
                }
            }
            DFA dfa = optimizedDFAs.get(dfaNo);
            helper.write(dfa.getName(), lexemeBuilder.toString());
        }
    }

    private void clear(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("");
        } catch (IOException ex) {
            System.err.println("Something wrong while clearing content of the file: " + file.getAbsolutePath());
        }
    }

    private static String trimLeading(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (s.charAt(i) != ' ') {
                return s.substring(i);
            }
        }
        return "";
    }
}
