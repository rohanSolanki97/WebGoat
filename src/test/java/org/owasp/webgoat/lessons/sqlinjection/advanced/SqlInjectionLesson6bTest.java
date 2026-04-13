package org.owasp.webgoat.lessons.sqlinjection.advanced;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.LessonDataSource;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.mockito.Mockito;

/**
 * Delta tests for SqlInjectionLesson6b focusing on:
 * - Ensuring getPassword() no longer prints stack traces to System.out/err when
 *   an exception occurs.
 * - Preserving completed() behavior for correct vs incorrect userid_6b inputs.
 */
class SqlInjectionLesson6bTest {

  @Test
  void getPassword_doesNotPrintStackTraceOnException() {
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionLesson6b lesson = new SqlInjectionLesson6b(dataSource);

    Mockito.when(dataSource.getConnection()).thenThrow(new RuntimeException("boom"));

    PrintStream originalOut = System.out;
    PrintStream originalErr = System.err;
    ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    System.setOut(new PrintStream(outContent));
    System.setErr(new PrintStream(errContent));

    try {
      lesson.getPassword();
    } finally {
      System.setOut(originalOut);
      System.setErr(originalErr);
    }

    String out = outContent.toString();
    String err = errContent.toString();

    assertEquals("", out);
    assertEquals("", err);
  }

  @Test
  void completed_behaviorUnchangedForCorrectAndIncorrectUserId() throws Exception {
    LessonDataSource dataSource = Mockito.mock(LessonDataSource.class);
    SqlInjectionLesson6b lesson = new SqlInjectionLesson6b(dataSource);

    SqlInjectionLesson6b spyLesson = Mockito.spy(lesson);
    Mockito.doReturn("secret").when(spyLesson).getPassword();

    AttackResult successResult = spyLesson.completed("secret");
    AttackResult failResult = spyLesson.completed("wrong");

    assertEquals(AttackResult.Status.SUCCESS, successResult.getStatus());
    assertEquals(AttackResult.Status.FAILURE, failResult.getStatus());
  }
}
