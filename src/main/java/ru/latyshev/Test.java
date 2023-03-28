package ru.latyshev;

import ru.latyshev.entities.CommandParsing;

import java.util.Arrays;

public class Test {
    public static void main(String[] args) {
        String s = "save \"unique voting test.dat\"";
        s = s.split("\"")[1];
        System.out.println(s);
    }
}
