package org.example;

import java.io.FileReader;
import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;


public class Main {
    public static void main(String[] args) {
        MyParser parser = new MyParser();
        parser.parse("input.txt");


    }
}