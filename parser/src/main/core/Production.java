package main.core;

import java.util.List;

public class Production {

    private final int id;
    private final String head;
    private final List<String> body;

    public Production(int id, String head, List<String> body) {
        this.id = id;
        this.head = head;
        this.body = body;
    }

    public int getId() {
        return id;
    }

    public String getHead() {
        return head;
    }

    public List<String> getBody() {
        return body;
    }

    String symbolAt(int i) {
        if (i >= body.size()) {
            throw new IllegalArgumentException();
        }
        return body.get(i);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(head);
        sb.append(" ->");
        for (String s : body) {
            sb.append(" ").append(s);
        }
        return sb.toString();
    }

}
