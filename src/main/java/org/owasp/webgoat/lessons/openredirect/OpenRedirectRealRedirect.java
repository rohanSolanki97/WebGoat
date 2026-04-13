/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.openredirect;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.util.UriComponentsBuilder;

/**
 * Provides a real 302 redirect for experimentation separate from assignment scoring.
 */
@Controller
public class OpenRedirectRealRedirect {

  @GetMapping("/OpenRedirect/realRedirect")
  public ModelAndView real(@RequestParam("url") String url) {
    // Remediation: Validate redirect targets against a whitelist or ensure it's an internal path.
    // Reject absolute URLs or paths containing traversal sequences.
    if (url == null || url.isBlank() || !url.startsWith("/") || url.contains("..")) {
      // Redirect to a safe default page or return an error view
      return new ModelAndView("redirect:/");
    }
    // Use UriComponentsBuilder to ensure proper encoding and prevent potential issues
    // with path manipulation, even if the initial check passes.
    return new ModelAndView("redirect:" + UriComponentsBuilder.fromPath(url).build().toUriString());
  }
}
