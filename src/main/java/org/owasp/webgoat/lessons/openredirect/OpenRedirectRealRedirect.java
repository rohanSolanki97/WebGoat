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
    // Validate the redirect URL to prevent open redirects
    if (isSafeRedirectUrl(url)) {
      return new ModelAndView("redirect:" + url);
    } else {
      // Redirect to a default safe page or return an error for unsafe URLs
      return new ModelAndView("redirect:/welcome.mvc"); // Redirect to a known safe internal page
    }
  }

  private boolean isSafeRedirectUrl(String url) {
    // For this lesson, only internal redirects are considered safe.
    // An internal URL must start with '/' and not contain path traversal sequences or external schemes.
    if (url == null || url.isBlank()) {
      return false;
    }
    // Ensure it starts with a single '/' and does not contain double slashes (e.g., //evil.com)
    // or backslashes, or path traversal sequences (../)
    return url.startsWith("/")
        && !url.contains("//")
        && !url.contains("\\")
        && !url.contains("..")
        && !url.matches("^[a-zA-Z]+://.*$"); // Reject explicit external schemes
  }
}
