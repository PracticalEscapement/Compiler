package org.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AbstractSyntaxTree {

    // MEMBER VAR FOR NodeProgram
    private NodeProgram nodeProgram;

    // Member for lines of code
    private List<String> linesCode = new ArrayList<>();

    // The current open register
    private int nextReg=1;

    // Hashmap to keep track of what data is assigned to specific register
    private Map<String, String> regMap = new HashMap<>();

    // gets the next open register, then increments the nextReg
    private String getNextOpenReg() {
        int reg = nextReg;
        this.nextReg++;
        return "ri"+ String.valueOf(reg);
    }

    public NodeProgram getNodeProgram() {

        return nodeProgram;
    }

    // Setter injection of nodeProgram class ie. NO CONSTRUCTOR
    public void setNodeProgram(NodeProgram nodeProgram) {
        this.nodeProgram = nodeProgram;
    }

    public void display() {
        nodeProgram.display();
    }

    // Formats the lines of code in the linesCode list to a single string
    public String getCode() {
        this.nodeProgram.generateCode();
        StringBuilder code = new StringBuilder();
        for (String line : linesCode) {
            code.append(line).append("\n");
        }
        return code.toString();
    }

    // All subclasses are below

    // Base class for all other nodes to inherit
    // All classes will need to implement display method bc of this base class
    // Base class where generateCode() method is defined

    // TODO what classes with have code generated?
    // ID, IF, Declarations, Sum

    public abstract class NodeBase {
        public abstract void display();
        public abstract String generateCode();
    }

    // TODO what should the display method show here (Nothing?)
    public abstract class NodeExpr extends NodeBase {
//        @Override
//        public void display() {
//
//        }
    }

    public abstract class NodeStmt extends NodeBase {
//        @Override
//        public void display() {
//
//        }
    }

    public class NodeId extends NodeExpr {
        String name;

        @Override
        public void display() {
            System.out.println("AST ID " + name);
        }

        @Override
        public String generateCode() {
            if (regMap.containsKey(name)) {
                return regMap.get(name);
            }
            String reg = getNextOpenReg();
            String line = String.format("var int %s", name);
            regMap.put(name, reg);
            linesCode.add(line);
            return reg;
        }
    }

    public class NodeIntLiteral extends NodeExpr {
        int intLiteral;

        @Override
        public void display() {
            System.out.println("AST int literal " + intLiteral);
        }

        @Override
        public String generateCode() {
            String reg = getNextOpenReg();
            String line = String.format("loadintliteral %s, %s", reg, intLiteral);
            linesCode.add(line);
            return reg;
        }
    }

    public class NodePlus extends NodeExpr {
        NodeExpr lhs;
        NodeExpr rhs;

        public NodePlus(NodeExpr lhs, NodeExpr rhs) {
            this.lhs = lhs;
            this.rhs = rhs;
        }

        @Override
        public void display() {
            System.out.println("AST SUM");
            System.out.println("AST LHS: " );
            lhs.display();
            System.out.println("AST RHS: ");
            rhs.display();
        }

        @Override
        public String generateCode() {
            String regLhs = this.lhs.generateCode();
            String regRhs = this.rhs.generateCode();
            String regResult = getNextOpenReg();
            String line = String.format("add %s, %s, %s", regLhs, regRhs, regResult);
            linesCode.add(line);
            return regResult;
        }
    }

    public class NodePrint extends NodeStmt {
        NodeId id;

        public NodePrint(NodeId id) {
            this.id = id;
        }

        @Override
        public void display() {
            System.out.println("AST Print");
            id.display();
            System.out.println();
        }

        // Get the reg or var to print from the map of regs
        @Override
        public String generateCode() {
            String idReg = regMap.get(id.name);

            String line = String.format("printi %s", id.name);
            linesCode.add(line);
            return idReg;
        }
    }

    public class NodeSet extends NodeStmt {
        NodeId id;
        NodeIntLiteral intLiteral;

        public NodeSet(NodeId id, NodeIntLiteral intLiteral) {
            this.id = id;
            this.intLiteral = intLiteral;
        }

        @Override
        public void display() {
            System.out.println("AST Set");
            id.display();
            intLiteral.display();
            System.out.println();
        }

        @Override
        public String generateCode() {
            String idReg = regMap.get(id.name);
            String loadLine = String.format("loadintliteral %s, %s", idReg, intLiteral.intLiteral);
            String storeLine = String.format("storeintvar %s, %s", idReg, id.name);
            linesCode.add(loadLine);
            linesCode.add(storeLine);
            return idReg;
        }
    }

    public class NodeCalc extends NodeStmt {
        NodeId id;
        NodeExpr expr;

        public NodeCalc(NodeId id, NodeExpr expr) {
            this.id = id;
            this.expr = expr;
        }

        @Override
        public void display() {
            System.out.println("AST Calc");
            id.display();
            expr.display();
            System.out.println();
        }

        @Override
        public String generateCode() {
            String calcResult = id.generateCode();
            String expression = expr.generateCode();
            String line = String.format("storeintvar %s, %s", expression, id.name);
            linesCode.add(line);
            return calcResult;
        }
    }

    public class NodeStmts extends NodeBase{
        List<NodeStmt> stmts = new ArrayList<>();

        public void addStmt(NodeStmt stmt) {
            stmts.add(stmt);
        }

        @Override
        public void display() {
            System.out.println("AST Statements");
            for (NodeStmt stmt : stmts) {
                stmt.display();
            }
            System.out.println();
        }

        @Override
        public String generateCode() {
            for (NodeStmt stmt : stmts) {
                stmt.generateCode();
            }
            return "";
        }
    }

    public class NodeIf extends NodeStmt {
        NodeId lhs;
        NodeId rhs;
        NodeStmts stmts;

        // Constructor lhs, rhs, stmts
        public NodeIf(NodeId lhs, NodeId rhs, NodeStmts stmts) {
            this.lhs = lhs;
            this.rhs = rhs;
            this.stmts = stmts;
        }

        @Override
        public void display() {
            System.out.println("AST if");
            System.out.print("LHS: ");
            lhs.display();
            System.out.print("RHS: ");
            rhs.display();
            stmts.display();
            System.out.println();
        }

        @Override
        public String generateCode() {
            String lhsReg = lhs.generateCode();
            String rhsReg = rhs.generateCode();
            String trueLabel = "equallabel";
            String falseLabel = "endTestlabel";
            String condition = String.format("be %s, %s, %s", lhsReg, rhsReg, trueLabel);
            linesCode.add(condition);
            linesCode.add("branch " + falseLabel);
            linesCode.add(":" + trueLabel);
            stmts.generateCode();
            linesCode.add(":" + falseLabel);
            return "";
        }
    }

    // All declarations should be within the .data section of the assembly program
    public class NodeDecls extends NodeBase{
        List<NodeId> decls = new ArrayList<>();

        public void addDecl(NodeId decl) {
            decls.add(decl);
        }

        @Override
        public void display() {
            System.out.println("AST Declarations");
            for (NodeId i : decls) {
                i.display();
            }
            System.out.println();
        }

        // .data will be at the top of the method followed by a loop that calls display on all the declarations
        @Override
        public String generateCode() {
            for (NodeId decl : decls) {
                decl.generateCode();
            }
            return "";
        }
    }

    public class NodeProgram extends NodeBase {
        NodeDecls decls;
        NodeStmts stmts;

        public NodeProgram(NodeDecls decls, NodeStmts stmts) {
            this.decls = decls;
            this.stmts = stmts;
        }

        @Override
        public void display() {
            decls.display();
            stmts.display();
        }

        @Override
        public String generateCode() {
            linesCode.add(".data");
            decls.generateCode();
            linesCode.add(".code");
            stmts.generateCode();
            return "";
        }

    }


}
