package org.example;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

public class MyParser {

    private enum Type {INTDATATYPE}

    private class SymbolTableItem {
        String name;
        Type type;
    }

    // MyScanner injected into MyParser class
    private MyScanner scanner;
    private MyScanner.Token nextToken;
    private AbstractSyntaxTree ast;

    // HashMap to check if variable declarations have already been made
    // Error must be thrown if there are duplicate declarations
    private Map<String, SymbolTableItem> symbolTable = new HashMap<>();

    // One token of lookahead
    // Adds declarations to map a.k.a symbolTable
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

    // Returns the Abstract instance tree
    public AbstractSyntaxTree getAst() {
        return ast;
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

    public void parse(String filename) {
        try {
            FileReader fr = new FileReader(filename);
            PushbackReader pbr = new PushbackReader(fr);
            scanner = new MyScanner(pbr);
            getNextToken();

            // Start symbol
            AbstractSyntaxTree abstractSyntaxTree = new AbstractSyntaxTree();
            this.ast = abstractSyntaxTree;

            // program is called, which recursively calls the terminal and non-terminal functions.
            abstractSyntaxTree.setNodeProgram(program());
            ast.display();

            // TODO This is where ast.generateCode() should be called.
            // save the contents of code into a file to later be executed by Hoskey library
            String code = ast.getCode();
            System.out.println(code);
            try (FileWriter fileWriter = new FileWriter("assemCode.txt")) {
                fileWriter.write(code);
            } catch (IOException e) {
                System.out.println("Failed");
            }


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


    // Starting production <Program> ::= <Decls> <Stmts> $
    private AbstractSyntaxTree.NodeProgram program() {

        // should be within .data
        AbstractSyntaxTree.NodeDecls nodeDecls = decls();
        // should be within .code
        AbstractSyntaxTree.NodeStmts nodeStmts = stmts();

        if (!match(MyScanner.Token.SCANEOF)) {
            System.out.println("Parsing Error: EOF Not Reached!");
        }
        System.out.println("Parsing Complete");

        return ast.new NodeProgram(nodeDecls, nodeStmts);
    }


    // <Decls> ::= <Decl> <Decls>
    // Non-terminal
    private AbstractSyntaxTree.NodeDecls decls() {
        AbstractSyntaxTree.NodeDecls nodeDecls = ast.new NodeDecls();
        while (nextToken == MyScanner.Token.DECLARE) {
            nodeDecls.addDecl(decl());
        }
        return nodeDecls;
    }

    // <Decl> ::= declare id
    // Terminal
    private AbstractSyntaxTree.NodeId decl() {
        AbstractSyntaxTree.NodeId nodeId = ast.new NodeId();
        if (nextToken == MyScanner.Token.DECLARE) {
            if (!match(MyScanner.Token.DECLARE)) {
                System.out.println("Parse Error: Expected declare");
                System.exit(0);
            }

            // Create the key to add to the Symbol Table
            String currentVarName = scanner.getTokenBufferString();
            nodeId.name = currentVarName;

            if (!match(MyScanner.Token.ID)) {
                System.out.println("Parse Error: Expected id");
                System.exit(0);
            }

            if (!match(MyScanner.Token.INTDATATYPE)) {
                System.out.println("Parse Error: Expected declare intdatatype");
                System.exit(0);
            }
            addDeclaration(currentVarName, Type.INTDATATYPE);
            return nodeId;
        }
        return nodeId;
    }

    // <Stmts> ::= <Stmt> <Stmts>
    private AbstractSyntaxTree.NodeStmts stmts() {
        AbstractSyntaxTree.NodeStmts nodeStmts = ast.new NodeStmts();
        while (nextToken == MyScanner.Token.PRINT ||
                nextToken == MyScanner.Token.SET ||
                nextToken == MyScanner.Token.IF ||
                nextToken == MyScanner.Token.CALC) {
            nodeStmts.addStmt(stmt());
        }
        return nodeStmts;
    }

    // check tokens for the various statements
    // Terminal unless there is an if statement --> another productions is needed for body of if statement
    private AbstractSyntaxTree.NodeStmt stmt() {
        if (nextToken == MyScanner.Token.PRINT) {
            if (!match(MyScanner.Token.PRINT)) {
                System.out.println("Parsing Error: Expected PRINT");
                System.exit(0);
            }

            AbstractSyntaxTree.NodeId nodeId = ast.new NodeId();
            nodeId.name = scanner.getTokenBufferString();

            if (!match(MyScanner.Token.ID)) {
                System.out.println("Parsing Error: Expected ID");
                System.exit(0);
            }

            return ast.new NodePrint(nodeId);
        }
        if (nextToken == MyScanner.Token.SET) {
            if (!match(MyScanner.Token.SET)) {
                System.out.println("Parsing Error: Expected SET");
                System.exit(0);
            }

            AbstractSyntaxTree.NodeId nodeId = ast.new NodeId();
            nodeId.name = scanner.getTokenBufferString();

            if (!match(MyScanner.Token.ID)) {
                System.out.println("Parsing Error: Expected ID");
                System.exit(0);
            }

            if (!match(MyScanner.Token.EQUALS)) {
                System.out.println("Parsing Error: Expected EQUALS");
                System.exit(0);
            }


            AbstractSyntaxTree.NodeIntLiteral intLiteral = ast.new NodeIntLiteral();
            intLiteral.intLiteral = Integer.parseInt(scanner.getTokenBufferString());

            if (!match(MyScanner.Token.INTLITERAL)) {
                System.out.println("Parsing Error: Expected INTLITERAL");
                System.exit(0);
            }
            return ast.new NodeSet(nodeId, intLiteral);
        }
        if (nextToken == MyScanner.Token.IF) {
            if (!match(MyScanner.Token.IF)) {
                System.out.println("Parsing Error: Expected IF");
                System.exit(0);
            }

            AbstractSyntaxTree.NodeId lhs = ast.new NodeId();
            lhs.name = scanner.getTokenBufferString();

            if (!match(MyScanner.Token.ID)) {
                System.out.println("Parsing Error: Expected ID");
                System.exit(0);
            }

            if (!match(MyScanner.Token.EQUALS)) {
                System.out.println("Parsing Error: Expected EQUALS");
                System.exit(0);
            }

            AbstractSyntaxTree.NodeId rhs = ast.new NodeId();
            rhs.name = scanner.getTokenBufferString();

            if (!match(MyScanner.Token.ID)) {
                System.out.println("Parsing Error: Expected ID");
                System.exit(0);
            }

            if (!match(MyScanner.Token.THEN)) {
                System.out.println("Parsing Error: Expected THEN");
                System.exit(0);
            }
            AbstractSyntaxTree.NodeStmts stmts = stmts();
            if (!match(MyScanner.Token.ENDIF)) {
                System.out.println("Parsing Error: Expected ENDIF");
                System.exit(0);
            }
            return ast.new NodeIf(lhs, rhs, stmts);
        }
        if (nextToken == MyScanner.Token.CALC) {
            if (!match(MyScanner.Token.CALC)) {
                System.out.println("Parsing Error: Expected CALC");
                System.exit(0);
            }

            AbstractSyntaxTree.NodeId nodeId = ast.new NodeId();
            nodeId.name = scanner.getTokenBufferString();

            if (!match(MyScanner.Token.ID)) {
                System.out.println("Parsing Error: Expected ID");
                System.exit(0);
            }

            if (!match(MyScanner.Token.EQUALS)) {
                System.out.println("Parsing Error: Expected EQUALS");
                System.exit(0);
            }
            AbstractSyntaxTree.NodeExpr expr = sum();
            return ast.new NodeCalc(nodeId, expr);
        }
        return null;
    }

    // <Sum> ::= <Value> <SumEnd>
    private AbstractSyntaxTree.NodeExpr sum() {
        AbstractSyntaxTree.NodeExpr lhs = value();
        return sumEnd(lhs);
    }

    // <Value> ::= id
    // <Value> ::= intliteral
    private AbstractSyntaxTree.NodeExpr value() {
        if (nextToken == MyScanner.Token.ID) {

            AbstractSyntaxTree.NodeId nodeId = ast.new NodeId();
            nodeId.name = scanner.getTokenBufferString();

            if (!match(MyScanner.Token.ID)) {
                System.out.println("Parsing Error: Expected ID");
                System.exit(0);
            }

            return nodeId;
        }

        if (nextToken == MyScanner.Token.INTLITERAL) {

            AbstractSyntaxTree.NodeIntLiteral nodeIntLiteral = ast.new NodeIntLiteral();
            nodeIntLiteral.intLiteral = Integer.parseInt(scanner.getTokenBufferString());

            if (!match(MyScanner.Token.INTLITERAL)) {
                System.out.println("Parsing Error: Expected INTLITERAL");
                System.exit(0);
            }
            return nodeIntLiteral;
        }
        return null;
    }

    //  <SumEnd> ::= + <Value> <SumEnd>
    //  <SumEnd> ::= ""
    private AbstractSyntaxTree.NodeExpr sumEnd(AbstractSyntaxTree.NodeExpr lhs) {
        if (nextToken == MyScanner.Token.PLUS) {
            if (!match(MyScanner.Token.PLUS)) {
                System.out.println("Parsing Error: Expected PLUS");
                System.exit(0);
            }
            AbstractSyntaxTree.NodeExpr rhs = value();
            lhs = ast.new NodePlus(lhs, rhs);
            return sumEnd(lhs);
        }
        return lhs;
    }

}
