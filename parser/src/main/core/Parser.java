package main.core;

import main.exception.InvalidFileFormatException;
import main.exception.InvalidFormatException;
import main.exception.InvalidNameException;
import main.exception.ParseException;
import main.util.*;
import main.core.action.AcceptAction;
import main.core.action.Action;
import main.core.action.ReduceAction;
import main.core.action.ShiftAction;
import main.util.io.LexerStub;

import java.io.*;
import java.util.*;
import java.util.regex.Pattern;

public class Parser {

    private static final Pattern VALID_NAME_PATTERN = Pattern.compile("[_a-zA-Z]\\w*");

    private DataPool pool;
    private IDGenerator generator;

    private List<Map<String, Action>> parseTable;
    private String startSymbol;

    public Parser(String inputFilePath) throws IOException {
        if (!inputFilePath.endsWith(".y")) {
            int extendStart = inputFilePath.lastIndexOf('.');
            String extendName = inputFilePath.substring(extendStart);
            throw new InvalidFileFormatException("Expected: .y, Actual: " + extendName);
        } else {
            File inputFile = new File(inputFilePath);
            if (!inputFile.exists()) {
                throw new FileNotFoundException(inputFilePath);
            } else {
                this.parseTable = new ArrayList<>();
                this.pool = new DataPool();
                this.generator = new IDGenerator();
                try {
                    init(new FileInputStream(inputFile));
                } catch (IOException ex) {
                    ex.printStackTrace();
                }

                DebugHelper.printProductions(pool.getHeadToProductions());
                DebugHelper.printProductions(pool.getHeadToProductions());

                pool.findNullableSymbols();
                constructParseTable();

                DebugHelper.printParseTable(pool.terminals(), pool.nonTerminals(), parseTable);
            }
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

    private void init(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        int lineNumber = 0;
        int section = SectionConstants.DECLARATION_SECTION;
        String line;
        while ((line = reader.readLine()) != null) {
            lineNumber++;
            // TODO : 检查源文件格式
            if (!"".equals(line.trim())) {
                if (line.startsWith(SectionConstants.DELIMITER) && SectionConstants.DELIMITER.equals(line.trim())) {
                    switch (section) {
                        case SectionConstants.DECLARATION_SECTION:
                            if (startSymbol == null) {
                                throw new InvalidFormatException("Start Symbols Not Found");
                            }
                            section = SectionConstants.RULE_SECTION;
                            break;
                        case SectionConstants.RULE_SECTION:
                        default:
                            throw new InvalidFileFormatException("More Than One Section-Delimiter '%%'");
                    }
                } else {
                    if (line.equals(trimLeading(line))) {
                        switch (section) {
                            case SectionConstants.DECLARATION_SECTION:
                                if (line.startsWith(SectionConstants.TOKEN)) {
                                    String[] tokenTokens = line.split("\\s+");
                                    for (int i = 1; i < tokenTokens.length; i++) {
                                        String tokenName = tokenTokens[i];
                                        if (isValidName(tokenName)) {
                                            pool.addToken(tokenName);
                                        } else {
                                            throw new InvalidNameException("\"" + tokenName + "\", Line: " + lineNumber);
                                        }
                                    }
                                } else if (line.startsWith(SectionConstants.START)) {
                                    if (startSymbol != null) {
                                        throw new InvalidFormatException(
                                                "A Yacc Source File Should Have Exactly One " +
                                                        "\"%start\" Statement, Line: " + lineNumber);
                                    }
                                    String[] startTokens = line.split("\\s+");
                                    if (startTokens.length != 2) {
                                        throw new InvalidFileFormatException(
                                                "A Grammar Should Have Exactly One " +
                                                        "Start Symbols, Line: " + lineNumber);
                                    } else {
                                        String startSymbolName = startTokens[1];
                                        if (isValidName(startSymbolName)) {
                                            startSymbol = startSymbolName;
                                        } else {
                                            throw new InvalidNameException("\"" + startSymbolName + "\", Line: " + lineNumber);
                                        }
                                    }
                                } else if (line.startsWith(SectionConstants.EMPTY)) {
                                    String[] emptyTokens = line.split("\\s+");
                                    if (emptyTokens.length < 2) {
                                        throw new InvalidFormatException("\"%empty\" " +
                                                "Should Be Followed By A Name, Line: " + lineNumber);
                                    }
                                    String emptySymbolName = emptyTokens[1];
                                    if (!isValidName(emptySymbolName)) {
                                        throw new InvalidNameException("\"" + emptySymbolName + "\", Line: " + lineNumber);
                                    } else {
                                        Symbols.EPSILON = emptySymbolName;
                                    }
                                }
                                break;
                            case SectionConstants.RULE_SECTION:
                                line = line.trim();
                                if (line.split("\\s+").length != 1) {
                                    throw new InvalidFormatException("The Beginning Of A Production Should Contain Only The Head's Name");
                                } else {
                                    if (!isValidName(line)) {
                                        throw new InvalidNameException("\"" + line + "\", Line: " + lineNumber);
                                    } else {
                                        String headName = line;
                                        pool.addNonTerminal(headName);
                                        line = reader.readLine().trim();
                                        if (line.startsWith(";")) {
                                            throw new InvalidFormatException("Production Doesn't Has A Body");
                                        } else if (!line.startsWith(":")) {
                                            throw new InvalidFormatException("Use ':' To Indicate The Body");
                                        } else {
                                            try {
                                                getAProduction(headName, line);
                                            } catch (InvalidNameException ex) {
                                                throw new InvalidNameException(ex.getMessage() + ", Line: " + lineNumber);
                                            }
                                        }
                                        while (true) {
                                            line = reader.readLine().trim();
                                            if (line.startsWith(";")) {
                                                break;
                                            } else if (!line.startsWith("|")) {
                                                throw new InvalidFormatException(
                                                        "Use '|' To Separate Alternatives, Line: " + lineNumber);
                                            } else {
                                                try {
                                                    getAProduction(headName, line);
                                                } catch (InvalidNameException ex) {
                                                    throw new InvalidNameException(
                                                            ex.getMessage() + ", Line: " + lineNumber);
                                                }
                                            }
                                        }
                                    }
                                }
                                break;
                            default:
                                assert true : "!!! invalid section number !!!";
                        }
                    } else {
                        throw new InvalidFormatException(
                                "Line Starts With White Space(s), Line: " + lineNumber);
                    }
                }
            }
        }
    }

    private void getAProduction(String headName, String line) throws InvalidNameException {
        String[] bodySymbols = line.substring(1).trim().split("\\s+");
        for (String s : bodySymbols) {
            if (!isValidName(s)) {
                throw new InvalidNameException("\"" + s + "\"");
            }
            if (!pool.tokenExists(s)) {
                pool.addNonTerminal(s);
            }
        }
        List<String> alternative = Arrays.asList(bodySymbols);
        Production p = new Production(generator.assignID(), headName, alternative);
        pool.addProduction(p.getHead(), p);
    }

    private void constructParseTable() {
        Production augmented = new Production(0, "S'", Collections.singletonList(startSymbol));
        pool.addProduction(augmented.getHead(), augmented);
        LRItem accept = new LRItem(augmented, "$", 1);
        // 内层：LR 项集
        // 外层：LR 项集集合
        List<Set<LRItem>> itemSetCollection = new ArrayList<>();
        Queue<Set<LRItem>> queue = new LinkedList<>();
        Set<LRItem> I0 = new HashSet<>(
                Collections.singletonList(
                        new LRItem(augmented, "$")));
        I0 = inStateExtend(I0);
        queue.offer(I0);
        itemSetCollection.add(I0);
        while (!queue.isEmpty()) {
            Set<LRItem> itemSet = queue.poll();
            Map<String, Action> actionTable = new HashMap<>();
            // 填充 reduce
            for (LRItem item : itemSet) {
                if (item.isReducible()) {
                    int productionId = pool.getIdByProduction(item.getProduction());
                    actionTable.put(item.getPredictiveSymbol(), new ReduceAction(productionId));
                }
            }
            for (String symbol : pool.getSymbols()) {
                Set<LRItem> I = new HashSet<>(inStateExtend(transition(itemSet, symbol)));
                // 填充 shift
                int nextStateNumber;
                if (!I.isEmpty()) {
                    if (!itemSetCollection.contains(I)) {
                        queue.offer(I);
                        itemSetCollection.add(I);
                    }
                    nextStateNumber = itemSetCollection.indexOf(I);
                    actionTable.put(symbol, new ShiftAction(nextStateNumber));
                }
            }
            if (itemSet.contains(accept)) {
                actionTable.put("$", new AcceptAction());
            }
            parseTable.add(actionTable);
        }
    }

    private Set<LRItem> inStateExtend(Set<LRItem> itemSet) {
        Set<LRItem> extended = new HashSet<>(itemSet);
        Set<LRItem> old;
        do {
            old = new HashSet<>(extended);
            for (LRItem item : old) {
                if (!item.isReducible()) {
                    String nextSymbol = item.nextSymbol();
                    Set<String> predictiveSymbols = LRItems.first(item.fromBeta(), pool);
                    Set<Production> productions = pool.productionsOf(nextSymbol);
                    for (Production p : productions) {
                        for (String predictiveSymbol : predictiveSymbols) {
                            extended.add(new LRItem(p, predictiveSymbol));
                        }
                    }
                }
            }
        } while (!old.equals(extended));
        return extended;
    }

    private Set<LRItem> transition(Set<LRItem> itemSet, String symbol) {
        Set<LRItem> toItemSet = new HashSet<>();
        for (LRItem item : itemSet) {
            LRItem toItem = LRItems.shiftDot(item, symbol);
            if (toItem != null) {
                toItemSet.add(toItem);
            }
        }
        return toItemSet;
    }

    private boolean isValidName(String name) {
        return VALID_NAME_PATTERN.matcher(name).matches();
    }

    public void parse(InputStream src) {
        File outputFile = new File("sample/1/parsing.out");
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
            parse(src, new FileOutputStream(outputFile));
        } catch (ParseException | IOException ex) {
            ex.printStackTrace();
        }
    }

    public void parse(InputStream src, OutputStream dst) throws ParseException, IOException {
        OutputStreamWriter writer = new OutputStreamWriter(dst);

        Stack<String> symbolStack = new Stack<>();
        Stack<Integer> stateStack = new Stack<>();
        stateStack.push(0);

        LexerStub stub = new LexerStub(src);
        if (stub.hasNextToken()) {
            int number = 1;
            String token = stub.nextToken();
            token = token.substring(1, token.indexOf(",") > 0 ? token.indexOf(",") : token.length() - 1);
            while (true) {
                int currentState = stateStack.peek();
                Action action = parseTable.get(currentState).get(token);
                if (action != null) {
                    if (action instanceof ReduceAction) {
                        ReduceAction reduceAction = (ReduceAction) action;
                        int productionID = reduceAction.getProductionId();
                        Production p = pool.getProductionById(productionID);
                        writer.write(number + ") " + p);
                        writer.write(System.lineSeparator());
                        writer.flush();
                        number++;
                        for (int i = 0; i < p.getBody().size(); i++) {
                            stateStack.pop();
                            symbolStack.pop();
                        }
                        symbolStack.push(p.getHead());
                        currentState = stateStack.peek();
                        ShiftAction gotoAction = (ShiftAction) parseTable.get(currentState).get(symbolStack.peek());
                        stateStack.push(gotoAction.getNextState());
                    } else {
                        ShiftAction shiftAction = (ShiftAction) action;
                        stateStack.push(shiftAction.getNextState());
                        symbolStack.push(token);
                        token = stub.nextToken();
                        if (token != null) {
                            token = token.substring(1, token.indexOf(",") > 0 ? token.indexOf(",") : token.length() - 1);
                        } else {
                            break;
                        }
                    }
                } else {
                    throw new ParseException();
                }
            }

            symbolStack.push("$");
            while (symbolStack.size() != 1 || !symbolStack.peek().equals(startSymbol)) {
                String peekSymbol = symbolStack.pop();
                int currentState = stateStack.peek();
                Action action = parseTable.get(currentState).get(peekSymbol);
                if (action instanceof ReduceAction) {
                    ReduceAction reduceAction = (ReduceAction) parseTable.get(currentState).get(peekSymbol);
                    int productionID = reduceAction.getProductionId();
                    Production p = pool.getProductionById(productionID);
                    writer.write(number + ") " + p);
                    writer.write(System.lineSeparator());
                    writer.flush();
                    number++;
                    for (int i = 0; i < p.getBody().size(); i++) {
                        stateStack.pop();
                        symbolStack.pop();
                    }
                    symbolStack.push(p.getHead());
                    currentState = stateStack.peek();
                    ShiftAction gotoAction = (ShiftAction) parseTable.get(currentState).get(symbolStack.peek());
                    stateStack.push(gotoAction.getNextState());
                    symbolStack.push("$");
                } else if (action instanceof AcceptAction) {
                    break;
                }
            }
        } else {
            throw new ParseException("No Tokens");
        }
    }

    private void clear(File file) {
        try (FileWriter writer = new FileWriter(file)) {
            writer.write("");
        } catch (IOException ex) {
            System.err.println("Something wrong while clearing content of the file: " + file.getAbsolutePath());
        }
    }

}
