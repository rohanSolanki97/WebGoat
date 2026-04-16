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
    // Fix: Validate redirect URL to prevent Open Redirect (CWE-601)
    // Only allow redirects to internal paths starting with '/' or specific whitelisted domains.
    // For this lesson, we enforce internal paths.
    if (url == null || !url.startsWith("/") || url.contains("//") || url.contains("\\?") || url.contains("\\#")) { // Basic validation for internal paths
      // Fallback to a safe default or error page
      return new ModelAndView("redirect:/welcome.mvc"); // Redirect to a safe internal page
    }
    return new ModelAndView("redirect:" + url);
  }
}
