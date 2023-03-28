package ru.latyshev;

import ru.latyshev.entities.CommandParsing;

import java.util.Arrays;

public class Test {
    public static void main(String[] args) {
        String s = "view -t=\"zero votes\" -v=\"unique voting test\"";
        System.out.println(Arrays.toString(CommandParsing.getTopicAndVoteNames(s)));
    }
}
