package org.example;

import org.junit.jupiter.api.Test;

import java.io.PushbackReader;
import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.*;

class MyScannerTest {

    // Helper method to make the test class more readable
    // This will return the 1 token per call of MyScanner.scan()
    private MyScanner.Token testHelper(MyScanner myScanner) {
        MyScanner.Token token = null;
        try {
            token = myScanner.scan();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return token;
    }

    // declare x int
    // This should return "DECLARE ID INTDATATYPE"
    @Test
    void test1() {
        String data = "declare x int";
        PushbackReader pbr = new PushbackReader(new StringReader(data));
        MyScanner myScanner = new MyScanner(pbr);
        assertEquals(testHelper(myScanner), MyScanner.Token.DECLARE);
        assertEquals(testHelper(myScanner), MyScanner.Token.ID);
        assertEquals(testHelper(myScanner), MyScanner.Token.INTDATATYPE);

    }

    // set x = 5
    // This should return "SET ID EQUALS INTLITERAL"
    @Test
    void test2() {
        String data = "set x = 5";
        PushbackReader pbr = new PushbackReader(new StringReader(data));
        MyScanner myScanner = new MyScanner(pbr);
        assertEquals(testHelper(myScanner), MyScanner.Token.SET);
        assertEquals(testHelper(myScanner), MyScanner.Token.ID);
        assertEquals(testHelper(myScanner), MyScanner.Token.EQUALS);
        assertEquals(testHelper(myScanner), MyScanner.Token.INTLITERAL);
    }

    // calc x + y
    // This should return "CALC ID PLUS ID"
    @Test
    void test3() {
        String data = "calc x + y";
        PushbackReader pbr = new PushbackReader(new StringReader(data));
        MyScanner myScanner = new MyScanner(pbr);
        assertEquals(testHelper(myScanner), MyScanner.Token.CALC);
        assertEquals(testHelper(myScanner), MyScanner.Token.ID);
        assertEquals(testHelper(myScanner), MyScanner.Token.PLUS);
        assertEquals(testHelper(myScanner), MyScanner.Token.ID);
    }

    // print x
    // This should return "PRINT ID"
    @Test
    void test4() {
        String data = "print x";
        PushbackReader pbr = new PushbackReader(new StringReader(data));
        MyScanner myScanner = new MyScanner(pbr);
        assertEquals(testHelper(myScanner), MyScanner.Token.PRINT);
        assertEquals(testHelper(myScanner), MyScanner.Token.ID);
    }

    // if x = y then \n endif
    // This should return "IF ID EQUALS ID THEN INTLITERAL"
    @Test
    void test5() {
        String data = "if x = y then \n endif";
        PushbackReader pbr = new PushbackReader(new StringReader(data));
        MyScanner myScanner = new MyScanner(pbr);
        assertEquals(testHelper(myScanner), MyScanner.Token.IF);
        assertEquals(testHelper(myScanner), MyScanner.Token.ID);
        assertEquals(testHelper(myScanner), MyScanner.Token.EQUALS);
        assertEquals(testHelper(myScanner), MyScanner.Token.ID);
        assertEquals(testHelper(myScanner), MyScanner.Token.THEN);
        assertEquals(testHelper(myScanner), MyScanner.Token.ENDIF);
    }

    // if x = y then\n print x \n endif
    // This should return "IF ID EQUALS ID THEN INTLITERAL PRINT ID INTLITERAL ENDIF"
    @Test
    void test6() {
        String data = "if x = y then\n print x \n endif";
        PushbackReader pbr = new PushbackReader(new StringReader(data));
        MyScanner myScanner = new MyScanner(pbr);
        assertEquals(testHelper(myScanner), MyScanner.Token.IF);
        assertEquals(testHelper(myScanner), MyScanner.Token.ID);
        assertEquals(testHelper(myScanner), MyScanner.Token.EQUALS);
        assertEquals(testHelper(myScanner), MyScanner.Token.ID);
        assertEquals(testHelper(myScanner), MyScanner.Token.THEN);
        assertEquals(testHelper(myScanner), MyScanner.Token.PRINT);
        assertEquals(testHelper(myScanner), MyScanner.Token.ID);
        assertEquals(testHelper(myScanner), MyScanner.Token.ENDIF);
    }

    // Empty program
    // This should return "SCANEOF"
    @Test
    void test7() {
        String data = "";
        PushbackReader pbr = new PushbackReader(new StringReader(data));
        MyScanner myScanner = new MyScanner(pbr);
        assertEquals(testHelper(myScanner), MyScanner.Token.SCANEOF);
    }


}