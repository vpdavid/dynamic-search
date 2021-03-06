package io.dvp.jpa.filter.el;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.function.Function;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class BinaryOperatorTest {

  BinaryOperator operator = new BinaryOperator("+", 10);
  Symbol left, right, middle;

  Function<String, Symbol> numFactory = number -> new IntegerOperand().copy(number).get();

  @BeforeEach
  void init() {
    left = numFactory.apply("12");
    right = numFactory.apply("9");
    middle = numFactory.apply("38");
  }

  @Test
  void copy() {
    assertValid("+");
    assertValid(" +");
    assertValid("    + \t\t ");
    assertFalse(operator.copy("a").isPresent());
    assertFalse(operator.copy("2").isPresent());
    assertFalse(operator.copy("++").isPresent());
  }

  void assertValid(String exp) {
    Optional<Symbol> op = operator.copy(exp);
    assertTrue(op.isPresent());
    assertNotSame(operator, op.get());
    assertSame(BinaryOperator.class, op.get().getClass());
    assertEquals("[null" + exp.trim() + "null]", op.get().toString());
  }

  @Test
  void merge() {
    assertEquals(operator, operator.merge(left));
    assertEquals(operator, operator.merge(right));
    assertEquals("[[12]+[9]]", operator.toString());

    Symbol op2 = operator.merge(new BinaryOperator("+", 0));
    assertNotEquals(op2, operator);
    assertEquals("[[[12]+[9]]+null]", op2.toString());
  }

  @Test
  void mergeHeavierOperator() {
    BinaryOperator heavier = new BinaryOperator("/", 20);
    operator.merge(left).merge(middle);
    assertSame(operator, operator.merge(heavier));
    assertSame(operator, operator.merge(right));
    assertEquals("[[12]+[[38]/[9]]]", operator.toString());
  }
}
