package org.example;

import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.sql.SQLOutput;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MyScanner {
    // Token Enum member
    enum Token {
        SCANEOF, ID, INTLITERAL, INTDATATYPE, DECLARE, PRINT, SET, EQUALS, IF, THEN, ENDIF, CALC, PLUS
    }

    // String Array to hold reserved words
    private List<String> reservedWords;

    // Pushback reader
    private PushbackReader pbr;

    // Token buffer
    private StringBuilder buffer;


    // One parameter constructor
    // Only accepts PushbackReader
    public MyScanner(PushbackReader pbr) {
        this.pbr = pbr;
        this.buffer = new StringBuilder();
        this.reservedWords = new ArrayList<>(Arrays.asList("declare", "int", "print", "set", "if", "then", "endif", "calc"));
    }

    public Token scan() throws Exception {
        // Remove previous contents of the buffer
        buffer.setLength(0);

        // use while loop to iterate through input stream
        int c = readNextChar();
        while (c != -1) {
            // Check for whitespace
            if (isWhiteSpace(c)) {
                c = readNextChar();
                continue;

            } else if (c == '+') {
                return Token.PLUS;
            } else if (c == '=') {
                return Token.EQUALS;

            } else if (Character.isDigit(c)) {
                buffer.append(Character.toString(c));
                c = readNextChar();
                while (Character.isDigit(c)) {
                    buffer.append(Character.toString(c));
                    c = readNextChar();
                }
                unRead(c);
                return Token.INTLITERAL;
            }
            // If the character is a letter, or letters add them to buffer
            else if (Character.isLetter(c)) {
                buffer.append(Character.toString(c));
                c = readNextChar();
                while (Character.isLetter(c)) {
                    buffer.append(Character.toString(c));
                    c = readNextChar();
                }
                unRead(c);

                // Analyze the buffer to determine if it's a reserved word
                if (!reservedWords.contains(getTokenBufferString())) {
                    return Token.ID;
                } else if (Objects.equals(getTokenBufferString(), "declare")) {
                    return Token.DECLARE;
                } else if (Objects.equals(getTokenBufferString(), "int")) {
                    return Token.INTDATATYPE;
                } else if (Objects.equals(getTokenBufferString(), "print")) {
                    return  Token.PRINT;
                } else if (Objects.equals(getTokenBufferString(), "set")) {
                    return  Token.SET;
                } else if (Objects.equals(getTokenBufferString(), "if")) {
                    return  Token.IF;
                } else if (Objects.equals(getTokenBufferString(), "then")) {
                    return  Token.THEN;
                } else if (Objects.equals(getTokenBufferString(), "endif")) {
                    return  Token.ENDIF;
                } else if (Objects.equals(getTokenBufferString(), "calc")) {
                    return  Token.CALC;
                }
            }
            c = -1;
        }
        unRead(c);
        return Token.SCANEOF;
    }

    // Figure out access modifier later
    public String getTokenBufferString() {
        return this.buffer.toString();
    }

    // Helper method to read next char
    private int readNextChar() {
        int c;
        try {
            c = pbr.read();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return c;
    }

    // Helper method to pushback char into input stream
    private void unRead(int c) {
        try {
            pbr.unread(c);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Helper method to remove whitespace
    private boolean isWhiteSpace(int c) {
        return (c == 32) || (c == 9) || (c == 10) || (c == 13);
    }








}
