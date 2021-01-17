package com.github.nicksetzer.metallurgy.orm.dsl;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class ParserBaseTest {

    @Test
    public void test_simple2() {

        List<Integer> seq = new ArrayList<>();

        seq.add(1);
        seq.add(2);
        seq.add(3);
        seq.add(4);
        seq.add(5);

        System.out.println(seq);
        List<Integer> sub = seq.subList(1, 4);

        List<Integer> children = new ArrayList<>(sub);
        sub.clear();

        System.out.println(children);
        System.out.println(seq);
    }
}
