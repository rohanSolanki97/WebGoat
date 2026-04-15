package org.owasp.webgoat.lessons.deserialization;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;
import java.util.Base64;
import org.dummy.insecure.framework.VulnerableTaskHolder;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.assignments.AttackResult;

public class InsecureDeserializationTaskTest {

  private final InsecureDeserializationTask task = new InsecureDeserializationTask();

  private String toWebGoatToken(Object obj) throws Exception {
    ByteArrayOutputStream bos = new ByteArrayOutputStream();
    try (ObjectOutputStream oos = new ObjectOutputStream(bos)) {
      oos.writeObject(obj);
    }
    String base64 = Base64.getEncoder().encodeToString(bos.toByteArray());
    return base64.replace('+', '-').replace('/', '_');
  }

  @Test
  public void completed_handlesPlainStringWithoutCompletion() throws Exception {
    String token = toWebGoatToken("just a string");

    AttackResult result = task.completed(token);

    assertThat(result.getLessonCompleted()).isFalse();
  }

  @Test
  public void completed_rejectsDisallowedType() throws Exception {
    class Disallowed implements java.io.Serializable {
      private static final long serialVersionUID = 1L;
    }
    String token = toWebGoatToken(new Disallowed());

    AttackResult result = task.completed(token);

    assertThat(result.getLessonCompleted()).isFalse();
  }

  @Test
  public void completed_controlsVulnerableTaskHolderBehavior() throws Exception {
    VulnerableTaskHolder holder = new VulnerableTaskHolder();
    String token = toWebGoatToken(holder);

    AttackResult result = task.completed(token);

    assertThat(result).isNotNull();
  }
}
