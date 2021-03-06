package io.dvp.jpa.filter.db;

import static io.dvp.jpa.filter.el.ExpressionTree.defaultSymbols;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.dvp.jpa.filter.el.ExpressionTree;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.Path;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class DatabaseBinderTest {

  @Mock
  Root<Article> root;
  @Mock
  CriteriaBuilder cb;
  @Mock
  Path<String> path1, path2;
  @Mock
  Path<Boolean> pathBool1, pathBool2;
  @Mock
  Predicate predicate1, predicate2, predicate3;

  DatabaseBinder<Article> databaseBinder;

  @Test
  void bindEqualToOperatorWithOneExpression() {
    when(root.<String>get("title")).thenReturn(path1);

    databaseBinder = new DatabaseBinder<>(root, cb, singletonMap("=", Mappers.equalTo()));

    ExpressionTree tree = ExpressionTree.build("{title}='Title1'", defaultSymbols());
    tree.getRoot().visit(databaseBinder);

    verify(cb).equal(eq(path1), eq("Title1"));
  }

  @Test
  void bindEqualToOperatorWithTwoExpression() {
    when(root.<String>get("title")).thenReturn(path1);
    when(root.<String>get("other")).thenReturn(path2);

    databaseBinder = new DatabaseBinder<>(root, cb, singletonMap("=", Mappers.equalTo()));

    ExpressionTree tree = ExpressionTree.build("{title}={other}", defaultSymbols());
    tree.getRoot().visit(databaseBinder);

    verify(cb).equal(eq(path1), eq(path2));
  }

  @Test
  void bindAndOperator() {
    when(root.<Boolean>get("active1")).thenReturn(pathBool1);
    when(root.<Boolean>get("active2")).thenReturn(pathBool2);

    databaseBinder = new DatabaseBinder<>(root, cb, singletonMap("and", Mappers.and()));

    ExpressionTree tree = ExpressionTree.build("{active1}and{active2}", defaultSymbols());
    tree.getRoot().visit(databaseBinder);

    verify(cb).and(eq(pathBool1), eq(pathBool2));
  }

  @Test
  void bindOrOperator() {
    when(root.<Boolean>get("active1")).thenReturn(pathBool1);
    when(root.<Boolean>get("active2")).thenReturn(pathBool2);

    databaseBinder = new DatabaseBinder<>(root, cb, singletonMap("or", Mappers.or()));

    ExpressionTree tree = ExpressionTree.build("{active1}or{active2}", defaultSymbols());
    tree.getRoot().visit(databaseBinder);

    verify(cb).or(eq(pathBool1), eq(pathBool2));
  }

  @Test
  void bindTwoOperatorsWithDifferentWeight() {
    when(root.<String>get("prop")).thenReturn(path1);
    when(root.<String>get("name")).thenReturn(path2);
    when(cb.equal(eq(path1), eq("something"))).thenReturn(predicate1);
    when(cb.equal(eq(path2), eq("me"))).thenReturn(predicate2);
    when(cb.and(eq(predicate1), eq(predicate2))).thenReturn(predicate3);

    Map<String, BiFunction<Deque<Object>, CriteriaBuilder, Predicate>> mappers = new HashMap<>();
    mappers.put("=", Mappers.equalTo());
    mappers.put("and", Mappers.and());
    databaseBinder = new DatabaseBinder<>(root, cb, mappers);

    String exp = "{prop}='something' and {name}='me'";
    ExpressionTree tree = ExpressionTree.build(exp, defaultSymbols());
    tree.getRoot().visit(databaseBinder);

    assertEquals(predicate3, databaseBinder.getPredicate());
  }
}
