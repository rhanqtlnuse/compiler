package main.core;

import main.exception.InvalidFormatException;
import main.exception.RegexException;

import java.util.*;

/**
 * 支持的符号
 *   转义：`\` `"`
 *     不同之处：`\` 比 `"` 更强，即 `\\` `\"` 合法，但 `"\"` 不合法
 *   范围：`[` `]` `-`
 *   量词：`?` `*`
 *   圆括号：`(` `)`
 *     用于确定优先级
 *   花括号：`{` `}`
 *     与 Lex 中相同，用于引用已定义的名字
 *   或运算：'|'
 *   任意字符：'.'
 *
 * 字符
 *   转义字符：`\n` `\t`
 *   英文字母
 *   数字
 *   下划线
 *
 * 每一行的形式如下：
 * name （若干空格） regular-expression
 * 其中 name 只允许使用英文字母 [a-zA-Z]
 */
public class Regex implements Iterable<String> {

    private static final char[] SUPPORTED_OPERATORS = new char[] {
            '\\', '"', '[', ']', '-', '?', '*', '(', ')', '{', '}', '.', '|'
    };

    private static Set<String> definedNames = new HashSet<>();
    private static Map<String, List<String>> nameToRegex = new HashMap<>();

    private String name;
    private List<String> regex;

    private Regex(String name, List<String> regex) {
        this.name = name;
        this.regex = regex;
    }

    public static Regex parse(String regexStr) throws RegexException, InvalidFormatException {
        regexStr = regexStr.trim().replaceAll("\\s+", " ");
        StringBuilder nameBuilder = new StringBuilder();
        int i = 0;
        char ch;
        while (i < regexStr.length() && (ch = regexStr.charAt(i)) != ' ') {
            if (Character.isLetter(ch)) {
                nameBuilder.append(ch);
                i++;
            } else {
                throw new RegexException("Invalid Symbol: " + ch + ", Column: " + (i + 1));
            }
        }
        if (i >= regexStr.length()) {
            throw new RegexException("Regular Expression Not Found");
        }
        if (definedNames.contains(nameBuilder.toString())) {
            throw new InvalidFormatException("Redefined Name: '" + nameBuilder + "'");
        }

        LinkedList<String> elements = new LinkedList<>();
        Stack<Character> balanceStack = new Stack<>();
        for (i = i+1; i < regexStr.length(); i++) {
            ch = regexStr.charAt(i);
            if (ch == '(') {
                balanceStack.push(ch);
                elements.add(String.valueOf(ch));
            } else if (ch == ')') {
                if (!balanceStack.isEmpty() && balanceStack.peek() == '(') {
                    balanceStack.pop();
                    elements.add(String.valueOf(ch));
                } else {
                    throw new RegexException("Unbalanced Parenthesis At " + (i+1));
                }
                if (i + 1 < regexStr.length()) {
                    char c = regexStr.charAt(i+1);
                    if (c != '?' && c != '*' && c != '|' && c != ')') {
                        elements.add("#");
                    }
                }
            } else if (ch == '[') {
                // 只允许 [a-z] 这种形式
                char[] chs = new char[4];
                if (i + 1 < regexStr.length()) {
                    chs[0] = regexStr.charAt(i+1);
                } else {
                    throw new RegexException("Non-Complete Range, Column: " + (i+1));
                }
                if (i + 2 < regexStr.length()) {
                    chs[1] = regexStr.charAt(i+2);
                } else {
                    throw new RegexException("Non-Complete Range, Column: " + (i+1));
                }
                if (i + 3 < regexStr.length()) {
                    chs[2] = regexStr.charAt(i+3);
                } else {
                    throw new RegexException("Non-Complete Range, Column: " + (i+1));
                }
                if (i + 4 < regexStr.length()) {
                    chs[3] = regexStr.charAt(i+4);
                } else {
                    throw new RegexException("Non-Complete Range, Column: " + (i+1));
                }
                if (chs[3] == ']') {
                    if (chs[1] == '-') {
                        if (typeOf(chs[0]) > 0 && typeOf(chs[2]) > 0) {
                            if (typeOf(chs[0]) == typeOf(chs[2])) {
                                if (chs[0] <= chs[2]) {
                                    elements.add("[" + chs[0] + "-" + chs[2] + "]");
                                    i = i + 4;
                                } else {
                                    throw new RegexException("Lower Bound Should be Less Than Or Equal To Upper Bound, Column: " + (i+1));
                                }
                            } else {
                                throw new RegexException("Lower Bound And Upper Bound Should Be The Same Type " +
                                        "(Upper Case/Lower Case/Digit), Column: " + (i+1));
                            }
                        } else {
                            throw new RegexException("Lower Bound And Upper Bound Should be Letters Or Digits, " +
                                    "Column: " + (i+1));
                        }
                    } else {
                        throw new InvalidFormatException("Column: " + (i+1) + ", Correct Format: [<c>-<c>]");
                    }
                } else {
                    throw new RegexException("Non-Complete Range, Column: " + (i+1));
                }
                if (i + 1 < regexStr.length()) {
                    char c = regexStr.charAt(i+1);
                    if (c != '?' && c != '*' && c != '|' && c != ')') {
                        elements.add("#");
                    }
                }
            } else if (ch == '{') {
                int j = i+1;
                StringBuilder sb = new StringBuilder();
                char c;
                while (j < regexStr.length() &&
                        Character.isLetter(c = regexStr.charAt(j))) {
                    sb.append(c);
                    j++;
                }
                if (j >= regexStr.length() || regexStr.charAt(j) != '}') {
                    throw new RegexException("Unbalanced Brace, Column: " + (i+1));
                } else {
                    String name = sb.toString();
                    if (definedNames.contains(name)) {
                        List<String> re = nameToRegex.get(name);
                        elements.add("(");
                        elements.addAll(re);
                        elements.add(")");
                    } else {
                        throw new RegexException("Undefined Name: " + name + ", Column: " + (i + 1 + 1));
                    }
                }
                i = j;
                if (i + 1 < regexStr.length()) {
                    c = regexStr.charAt(i+1);
                    if (c != '?' && c != '*' && c != '|' && c != ')') {
                        elements.add("#");
                    }
                }
            } else if (ch == '"') {
                int j = i+1;
                char c;
                List<String> components = new ArrayList<>();
                while (j < regexStr.length()
                        && (c = regexStr.charAt(j)) != '"') {
                    if (c == '\\') {
                        if (j + 1 < regexStr.length()) {
                            char c2 = regexStr.charAt(j + 1);
                            if (isSupported(c2)) {
                                components.add("\"\\" + c2 + "\"");
                                if (j + 2 < regexStr.length() && regexStr.charAt(j+2) != '"') {
                                    components.add("#");
                                }
                            } else if (c2 == 'n') {
                                components.add("\"\n\"");
                                if (j + 2 < regexStr.length() && regexStr.charAt(j+2) != '"') {
                                    components.add("#");
                                }
                            } else if (c2 == 't') {
                                components.add("\"\t\"");
                                if (j + 2 < regexStr.length() && regexStr.charAt(j+2) != '"') {
                                    components.add("#");
                                }
                            } else {
                                throw new RegexException("Unsupported Escape Character '\\" + c2 + "', Column: " + (i+1));
                            }
                            j = j + 1;
                        } else {
                            throw new RegexException("Back Slash Mustn't Appear At The End Of A Line, Column: " + (i+1));
                        }
                    } else {
                        components.add("\"" + String.valueOf(c) + "\"");
                        if (j + 1 < regexStr.length() && regexStr.charAt(j+1) != '"') {
                            components.add("#");
                        }
                    }
                    j++;
                }
                if (j >= regexStr.length()) {
                    throw new RegexException("Unbalanced Quote Mark, Column: " + (i+1));
                }
                i = j;
                elements.addAll(components);
                if (i + 1 < regexStr.length()) {
                    c = regexStr.charAt(i+1);
                    if (c != '?' && c != '*' && c != '|' && c != ')') {
                        elements.add("#");
                    }
                }
            } else if (ch == '\\') {
                if (i + 1 < regexStr.length()) {
                    char c = regexStr.charAt(i+1);
                    if (isSupported(c)) {
                        elements.add("\\" + c);
                    } else if (c == 'n') {
                        elements.add(String.valueOf("\n"));
                    } else if (c == 't') {
                        elements.add(String.valueOf("\t"));
                    } else {
                        throw new RegexException("Unsupported Escape Character '\\" + c + "', Column: " + (i+1));
                    }
                } else {
                    throw new RegexException("Back Slash Mustn't Appear At The End Of A Line, Column: " + (i+1));
                }
                i = i+1;
                if (i + 1 < regexStr.length()) {
                    char c = regexStr.charAt(i+1);
                    if (c != '?' && c != '*' && c != '|' && c != ')') {
                        elements.add("#");
                    }
                }
            } else if (ch == '?') {
                // 不是基本运算符，需要转写
                // <exp>? 转写为 (epsilon|<exp>)
                if (elements.size() > 0) {
                    LinkedList<String> subExp = new LinkedList<>();
                    if (")".equals(elements.peekLast())) {
                        Stack<String> st = new Stack<>();
                        String last = elements.pollLast();
                        subExp.addFirst(last);
                        st.push(last);
                        while (!st.isEmpty() && !elements.isEmpty()) {
                            last = elements.pollLast();
                            subExp.addFirst(last);
                            if (")".equals(last)) {
                                st.push(last);
                            } else if ("(".equals(last)) {
                                st.pop();
                            }
                        }
                        if (elements.isEmpty() && !st.isEmpty()) {
                            throw new RegexException("Unbalanced Brace, Column: " + (i+1));
                        }
                        subExp.addFirst("|");
                        subExp.addFirst(String.valueOf(Symbol.EPSILON));
                        subExp.addFirst("(");
                        subExp.add(")");
                        elements.addAll(subExp);
                    } else if (elements.peekLast().endsWith("]")) {
                        subExp.add("(");
                        subExp.add(String.valueOf(Symbol.EPSILON));
                        subExp.add("|");
                        subExp.add(elements.pollLast());
                        subExp.add(")");
                        elements.addAll(subExp);
                    } else if (elements.peekLast().endsWith("\"")) {
                        subExp.add("(");
                        subExp.add(String.valueOf(Symbol.EPSILON));
                        subExp.add("|");
                        subExp.add(elements.pollLast());
                        subExp.add(")");
                        elements.addAll(subExp);
                    } else if (elements.peekLast().startsWith("\\")) {
                        subExp.add("(");
                        subExp.add(String.valueOf(Symbol.EPSILON));
                        subExp.add("|");
                        subExp.add(elements.pollLast());
                        subExp.add(")");
                        elements.addAll(subExp);
                    } else if ("\n".equals(elements.peekLast())) {
                        subExp.add("(");
                        subExp.add(String.valueOf(Symbol.EPSILON));
                        subExp.add("|");
                        subExp.add(elements.pollLast());
                        subExp.add(")");
                        elements.addAll(subExp);
                    } else if ("\t".equals(elements.peekLast())) {
                        subExp.add("(");
                        subExp.add(String.valueOf(Symbol.EPSILON));
                        subExp.add("|");
                        subExp.add(elements.pollLast());
                        subExp.add(")");
                        elements.addAll(subExp);
                    } else if (Character.isLetterOrDigit(elements.peekLast().charAt(0))
                            || elements.peekLast().charAt(0) == '_'
                            || elements.peekLast().charAt(0) == '.') {
                        subExp.add("(");
                        subExp.add(String.valueOf(Symbol.EPSILON));
                        subExp.add("|");
                        subExp.add(elements.pollLast());
                        subExp.add(")");
                        elements.addAll(subExp);
                    } else {
                        throw new RegexException("Invalid: '" + ch + "' Column: " + (i+1));
                    }
                } else {
                    throw new RegexException("Invalid: '" + ch + "' Column: " + (i+1));
                }
                if (i + 1 < regexStr.length()) {
                    char c = regexStr.charAt(i+1);
                    if (c != '?' && c != '*' && c != '|' && c != ')') {
                        elements.add("#");
                    }
                }
            } else if (ch == '*') {
                elements.add(String.valueOf(ch));
                if (i + 1 < regexStr.length()) {
                    char c = regexStr.charAt(i+1);
                    if (c != '?' && c != '*' && c != '|' && c != ')') {
                        elements.add("#");
                    }
                }
            } else if (ch == '.') {
                elements.add(String.valueOf(ch));
                if (i + 1 < regexStr.length()) {
                    char c = regexStr.charAt(i+1);
                    if (c != '?' && c != '*' && c != '|' && c != ')') {
                        elements.add("#");
                    }
                }
            } else if (ch == '|') {
                elements.add(String.valueOf(ch));
            } else {
                if (Character.isLetter(ch)
                        || Character.isDigit(ch)
                        || ch == '_') {
                    elements.add(String.valueOf(ch));
                    if (i + 1 < regexStr.length()) {
                        char c = regexStr.charAt(i+1);
                        if (c != '?' && c != '*' && c != '|' && c != ')') {
                            elements.add("#");
                        }
                    }
                } else {
                    throw new RegexException("Invalid Symbol: " + ch + ", Column: " + (i+1));
                }
            }
        }

        definedNames.add(nameBuilder.toString());
        nameToRegex.put(nameBuilder.toString(), elements);

        if (!balanceStack.isEmpty()) {
            throw new RegexException("Unbalanced Brackets");
        }

        return new Regex(nameBuilder.toString(), elements);
    }

    private static int typeOf(char ch) {
        if ('a' <= ch && ch <= 'z') {
            return 1;
        } else if ('A' <= ch && ch <= 'Z') {
            return 2;
        } else if ('0' <= ch && ch <= '9') {
            return 3;
        } else {
            return -1;
        }
    }

    private static boolean isSupported(char operator) {
        for (char c : SUPPORTED_OPERATORS) {
            if (c == operator) {
                return true;
            }
        }
        return false;
    }

    public List<String> getRegex() {
        return new LinkedList<>(regex);
    }

    public String getName() {
        return name;
    }

    @Override
    public Iterator<String> iterator() {
        return regex.iterator();
    }

    @Override
    public String toString() {
        return "Regex{ " + name + " " + regex + " }";
    }
}
