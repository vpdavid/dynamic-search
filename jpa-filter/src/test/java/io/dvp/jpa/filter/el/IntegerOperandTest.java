package io.dvp.jpa.filter.el;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import org.junit.jupiter.api.Test;

public class IntegerOperandTest {

  IntegerOperand number = new IntegerOperand();

  @Test
  void copy() {
    Optional<Symbol> other = number.copy("234");
    assertTrue(other.isPresent());
    assertSame(other.get().getClass(), number.getClass());
    assertNotSame(other.get(), number);
    assertEquals("[234]", other.get().toString());

    assertFalse(number.copy("234.33").isPresent());
    assertFalse(number.copy("a").isPresent());
    assertFalse(number.copy(" 12 ").isPresent());
    assertFalse(number.copy("").isPresent());
  }
}
