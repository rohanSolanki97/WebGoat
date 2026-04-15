package org.owasp.webgoat.lessons.cryptography;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.http.HttpServletRequest;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.xml.bind.DatatypeConverter;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.owasp.webgoat.container.assignments.AttackResult;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletRequest;

public class HashingAssignmentTest {

  private final HashingAssignment hashingAssignment = new HashingAssignment();

  @Test
  public void getMd5_cachesHashInSession() throws NoSuchAlgorithmException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setMethod("GET");
    request.setRequestURI("/crypto/hashing/md5");
    request.setContentType(MediaType.TEXT_HTML_VALUE);

    String first = hashingAssignment.getMd5(request);

    String sessionHash = (String) request.getSession().getAttribute("md5Hash");
    String sessionSecret = (String) request.getSession().getAttribute("md5Secret");

    assertThat(first).isNotNull();
    assertThat(sessionHash).isEqualTo(first);
    assertThat(sessionSecret).isNotNull();
    assertThat(sessionSecret).isIn(HashingAssignment.SECRETS);

    String second = hashingAssignment.getMd5(request);
    assertThat(second).isEqualTo(first);
  }

  @Test
  public void getSha256_cachesHashAndSecretInSession() throws NoSuchAlgorithmException {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.setMethod("GET");
    request.setRequestURI("/crypto/hashing/sha256");
    request.setContentType(MediaType.TEXT_HTML_VALUE);

    String sha256First = hashingAssignment.getSha256(request);

    String sha256Hash = (String) request.getSession().getAttribute("sha256Hash");
    String sha256Secret = (String) request.getSession().getAttribute("sha256Secret");

    assertThat(sha256First).isNotNull();
    assertThat(sha256Hash).isEqualTo(sha256First);
    assertThat(sha256Secret).isNotNull();
    assertThat(sha256Secret).isIn(HashingAssignment.SECRETS);

    String sha256Second = hashingAssignment.getSha256(request);
    assertThat(sha256Second).isEqualTo(sha256First);
  }

  @Test
  public void getHash_isDeterministic() throws NoSuchAlgorithmException {
    String secret = "test-secret";

    String hash1 = HashingAssignment.getHash(secret, "SHA-256");
    String hash2 = HashingAssignment.getHash(secret, "SHA-256");

    assertThat(hash1).isEqualTo(hash2);
    DatatypeConverter.parseHexBinary(hash1);
  }

  @Test
  public void endpoints_hashesMatchSessionSecrets() throws Exception {
    MockHttpServletRequest request = new MockHttpServletRequest();

    String md5Hash = hashingAssignment.getMd5(request);
    String md5Secret = (String) request.getSession().getAttribute("md5Secret");
    assertThat(md5Secret).isIn(HashingAssignment.SECRETS);

    String expectedMd5 =
        DatatypeConverter.printHexBinary(
                MessageDigest.getInstance("MD5").digest(md5Secret.getBytes()))
            .toUpperCase();
    assertThat(md5Hash).isEqualTo(expectedMd5);

    String sha256Hash = hashingAssignment.getSha256(request);
    String sha256Secret = (String) request.getSession().getAttribute("sha256Secret");
    assertThat(sha256Secret).isIn(HashingAssignment.SECRETS);

    String expectedSha256 = HashingAssignment.getHash(sha256Secret, "SHA-256");
    assertThat(sha256Hash).isEqualTo(expectedSha256);
  }

  @Test
  public void completed_requiresBothSecretsToMatch() {
    MockHttpServletRequest request = new MockHttpServletRequest();
    request.getSession().setAttribute("md5Secret", "secret1");
    request.getSession().setAttribute("sha256Secret", "secret2");

    AttackResult resultSuccess =
        hashingAssignment.completed(request, "secret1", "secret2");
    AttackResult resultOneOk =
        hashingAssignment.completed(request, "secret1", "wrong");
    AttackResult resultEmpty =
        hashingAssignment.completed(request, "wrong1", "wrong2");

    assertThat(resultSuccess.getLessonCompleted()).isTrue();
    assertThat(resultOneOk.getLessonCompleted()).isFalse();
    assertThat(resultEmpty.getLessonCompleted()).isFalse();
  }
}
