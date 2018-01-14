package fko.jarkanoid.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameModelTest {

  @BeforeEach
  void setUp() {}

  @Test
  void someTest() {

    int i = 0;
    System.out.println(i);
    System.out.println(Integer.toBinaryString(i));

    i = i | 1;
    System.out.println(i);
    System.out.println(Integer.toBinaryString(i));

//    i = i | 2;
//    System.out.println(i);
//    System.out.println(Integer.toBinaryString(i));

    i = i | 4;
    System.out.println(i);
    System.out.println(Integer.toBinaryString(i));

    i = i | 8;
    System.out.println(i);
    System.out.println(Integer.toBinaryString(i));

//    System.out.println("OR  " + (i | 15));
//    System.out.println("AND " + (i & 15));
//    System.out.println("XOR " + (i ^ 15));
//    System.out.println("COMP" + (~i));
//
    System.out.println("bits:  "+Integer.bitCount(i));
    System.out.println("binary:"+Integer.toBinaryString(i));
  }
}