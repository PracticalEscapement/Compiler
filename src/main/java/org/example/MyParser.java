package org.example;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PushbackReader;
import java.util.HashMap;
import java.util.Map;

public class MyParser {

    private enum Type {INTDATATYPE}

    private class SymbolTableItem {
        String name;
        Type type;
    }

    private void addDeclaration(String name, Type type) {
        if (symbolTable.containsKey(name)) {
            System.out.println("Error: Duplicate declaration of variable " + name);
            System.exit(0);
        } else {
            SymbolTableItem item = new SymbolTableItem();
            item.name = name;
            item.type = type;
            symbolTable.put(name, item);
        }
    }

    // HashMap to check if variable declarations have already been made
    // Error must be thrown if there are duplicate declarations
    private Map<String, SymbolTableItem> symbolTable = new HashMap<>();

    // MyScanner injected into MyParser class
    private MyScanner scanner;

    // One token of lookahead
    private MyScanner.Token nextToken;

    public void parse(String filename) {
        try {
            FileReader fr = new FileReader(filename);
            PushbackReader pbr = new PushbackReader(fr);
            scanner = new MyScanner(pbr);
            getNextToken();

            // Start symbol
            program();

            if (match(MyScanner.Token.SCANEOF)) {
                System.out.println("Success");
            } else {
                System.out.println("Failed");
                System.out.println(scanner.getTokenBufferString());
            }

        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

    }

    private void getNextToken() {
        try {
            nextToken = scanner.scan();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private boolean match(MyScanner.Token expectedToken) {
        if (nextToken == expectedToken) {
            getNextToken();
            return true;
        }
        System.out.println("Error Matching Token");
        return false;
    }

    // Starting production <Program> ::= <Decls> <Stmts> $
    private void program() {
        decls();
        stmts();
        if (!match(MyScanner.Token.SCANEOF)) {
            System.out.println("Parsing Error: EOF Not Reached!");
        }
        System.out.println("Parsing Complete");
    }

    // <Decls> ::= <Decl> <Decls>
    // Non-terminal
    private void decls() {
        if (nextToken == MyScanner.Token.DECLARE) {
            decl();
            decls();
        }
    }

    // <Decl> ::= declare id
    // Terminal
    private void decl() {
        if (nextToken == MyScanner.Token.DECLARE) {
            if (!match(MyScanner.Token.DECLARE)) {
                System.out.println("Parse Error: Expected declare");
                System.exit(0);
            }

            // Create the key to add to the Symbol Table
            String currentVarName = scanner.getTokenBufferString();

            if (!match(MyScanner.Token.ID)) {
                System.out.println("Parse Error: Expected id");
                System.exit(0);
            }
            if (!match(MyScanner.Token.INTDATATYPE)) {
                System.out.println("Parse Error: Expected declare intdatatype");
                System.exit(0);
            }
            addDeclaration(currentVarName, Type.INTDATATYPE);
        }
    }

    // <Stmts> ::= <Stmt> <Stmts>
    private void stmts() {
        if (nextToken == MyScanner.Token.PRINT ||
                nextToken == MyScanner.Token.SET ||
                nextToken == MyScanner.Token.IF ||
                nextToken == MyScanner.Token.CALC) {
            stmt();
            stmts();
        }
    }

    // check tokens for the various statements
    // Terminal unless there is an if statement --> another productions is needed for body of if statement
    private void stmt() {
        if (nextToken == MyScanner.Token.PRINT) {
            if (!match(MyScanner.Token.PRINT)) {
                System.out.println("Parsing Error: Expected PRINT");
                System.exit(0);
            }
            if (!match(MyScanner.Token.ID)) {
                System.out.println("Parsing Error: Expected ID");
                System.exit(0);
            }
        }
        if (nextToken == MyScanner.Token.SET) {
            if (!match(MyScanner.Token.SET)) {
                System.out.println("Parsing Error: Expected SET");
                System.exit(0);
            }
            if (!match(MyScanner.Token.ID)) {
                System.out.println("Parsing Error: Expected id");
                System.exit(0);
            }
            if (!match(MyScanner.Token.EQUALS)) {
                System.out.println("Parsing Error: Expected EQUALS");
                System.exit(0);
            }
            if (!match(MyScanner.Token.INTLITERAL)) {
                System.out.println("Parsing Error: Expected INTLITERAL");
                System.exit(0);
            }
        }
        if (nextToken == MyScanner.Token.IF) {
            if (!match(MyScanner.Token.IF)) {
                System.out.println("Parsing Error: Expected IF");
                System.exit(0);
            }
            if (!match(MyScanner.Token.ID)) {
                System.out.println("Parsing Error: Expected ID");
                System.exit(0);
            }
            if (!match(MyScanner.Token.EQUALS)) {
                System.out.println("Parsing Error: Expected EQUALS");
                System.exit(0);
            }
            if (!match(MyScanner.Token.ID)) {
                System.out.println("Parsing Error: Expected ID");
                System.exit(0);
            }
            if (!match(MyScanner.Token.THEN)) {
                System.out.println("Parsing Error: Expected THEN");
                System.exit(0);
            }
            stmts();
            if (!match(MyScanner.Token.ENDIF)) {

            }

        }
        if (nextToken == MyScanner.Token.CALC) {
            if (!match(MyScanner.Token.CALC)) {
                System.out.println("Parsing Error: Expected CALC");
                System.exit(0);
            }
            if (!match(MyScanner.Token.ID)) {
                System.out.println("Parsing Error: Expected ID");
                System.exit(0);
            }
            if (!match(MyScanner.Token.EQUALS)) {
                System.out.println("Parsing Error: Expected EQUALS");
                System.exit(0);
            }
            // Call to sum non-terminal
            sum();

        }
    }

    // <Sum> ::= <Value> <SumEnd>
    private void sum() {
        value();
        sumEnd();
    }

    // <Value> ::= id
    // <Value> ::= intliteral
    private void value() {
        if (nextToken == MyScanner.Token.ID) {
            if (!match(MyScanner.Token.ID)) {
                System.out.println("Parsing Error: Expected ID");
                System.exit(0);
            }
        }
        if (nextToken == MyScanner.Token.INTLITERAL) {
            if (!match(MyScanner.Token.INTLITERAL)) {
                System.out.println("Parsing Error: Expected INTLITERAL");
                System.exit(0);

            }
        }
    }

    //  <SumEnd> ::= + <Value> <SumEnd>
    //  <SumEnd> ::= ""
    private void sumEnd() {
        if (nextToken == MyScanner.Token.PLUS) {
            if (!match(MyScanner.Token.PLUS)) {
                System.out.println("Parsing Error: Expected PLUS");
                System.exit(0);
            }
            value();
            sumEnd();
        }
    }

}
