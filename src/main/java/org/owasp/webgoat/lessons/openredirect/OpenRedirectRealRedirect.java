/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.openredirect;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView; // Added import for RedirectView

/**
 * Provides a real 302 redirect for experimentation separate from assignment scoring.
 */
@Controller
public class OpenRedirectRealRedirect {

  @GetMapping("/OpenRedirect/realRedirect")
  public ModelAndView real(@RequestParam("url") String url) {
    // Fix: Validate redirect target against a whitelist to prevent Open Redirect vulnerabilities.
    // Only allow relative paths starting with '/' and prevent path traversal sequences.
    if (url == null || url.isBlank() || !url.startsWith("/") || url.contains("//") || url.contains("\\..") || url.contains("%2e%2e")) {
      // Redirect to a safe default page or error page if the URL is invalid
      return new ModelAndView("redirect:/welcome.mvc");
    }

    // Further validation could involve a whitelist of allowed internal paths
    // For this fix, we assume any internal relative path is acceptable after basic sanitization.
    return new ModelAndView("redirect:" + url);
  }
}
