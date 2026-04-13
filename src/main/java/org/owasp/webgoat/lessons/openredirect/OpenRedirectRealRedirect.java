/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.openredirect;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Provides a real 302 redirect for experimentation separate from assignment scoring.
 */
@Controller
public class OpenRedirectRealRedirect {

  @GetMapping("/OpenRedirect/realRedirect")
  public ModelAndView real(@RequestParam("url") String url) {
    // Remediation: Validate the URL to prevent open redirect.
    // For this lesson, only relative paths within the application context are allowed.
    // Absolute URLs, protocol-relative URLs (e.g., //evil.com), and URLs with schemes (e.g., javascript:alert(1)) are blocked.
    if (url != null && url.startsWith("/") && !url.startsWith("//") && !url.contains(":") && !url.contains("\\")) {
      // Further validation could involve a whitelist of allowed internal paths or specific regex patterns.
      return new ModelAndView("redirect:" + url);
    } else {
      // If the URL is not valid, redirect to a safe default page to prevent malicious redirects.
      return new ModelAndView("redirect:/welcome.mvc"); // Redirect to a known safe page
    }
  }
}
