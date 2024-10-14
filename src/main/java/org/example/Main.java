package org.example;

import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;


public class Main {
    public static void main(String[] args) {
        try {
            String data = "declare x";

            PushbackReader pbr = new PushbackReader(new StringReader(data));
            MyScanner labScanner = new MyScanner(pbr);

            MyScanner.Token token = null;

            while (token != MyScanner.Token.SCANEOF) {
                token = labScanner.scan();
                System.out.println(token);
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }


    }
}