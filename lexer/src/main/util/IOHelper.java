package main.util;

import java.io.*;

public class IOHelper {

    private BufferedReader reader;
    private OutputStreamWriter writer;
    private int readPtr;
    private int bound;
    private String[] buf;

    public IOHelper(InputStream src, OutputStream dest) {
        this.reader = new BufferedReader(new InputStreamReader(src));
        this.writer = new OutputStreamWriter(dest);
        this.readPtr = 0;
        this.bound = 0;
    }

    public String getBlock() throws IOException {
        if (readPtr >= bound) {
            String line = reader.readLine();
            if (line != null && !"".equals(line.trim())) {
                buf = line.trim().split("\\s+");
                bound = buf.length;
                readPtr = 0;
            } else {
                return "";
            }
        }
        return buf[readPtr++];
    }

    public void write(String catalog, int innerCode, String lexeme) throws IOException {
        writer.write("<" + catalog + ", " + innerCode + ", " + lexeme + ">");
        writer.write(System.lineSeparator());
        writer.flush();
    }

    public void write(String catalog, String lexeme) throws IOException {
        writer.write("<catalog: " + catalog + ", lexeme: \"" + lexeme + "\">");
        writer.write(System.lineSeparator());
        writer.flush();
    }
}
