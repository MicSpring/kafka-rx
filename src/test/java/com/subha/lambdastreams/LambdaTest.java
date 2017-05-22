package com.subha.lambdastreams;

import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

/**
 * Created by user on 5/14/2017.
 */
public class LambdaTest {
   static Func1<String, Integer> stringIntegerFunction = s -> s.length();

    List<String> list = Arrays.asList("Mickey", "Mic", "Donald");

    @Test
    public void getLength() {
       Assert.assertEquals(stringIntegerFunction.transform("Subha"), new Integer(5));
    }

    @Test
    public void getEach(){
        list.stream().forEach(s -> System.out.println(s));
        list.stream().forEach(this::printEach);
    }

    public void printEach(String str){
        System.out.println(str);
    }

    public static Integer getMeLength(Func1<String, Integer> func1, String str){
        return func1.transform(str);
    }
}
