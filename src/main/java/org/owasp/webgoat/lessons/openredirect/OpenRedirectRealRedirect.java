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
    // Validate redirect target against a whitelist or enforce relative paths
    // Reject absolute URLs unless explicitly validated against a whitelist
    // For this lesson, we enforce that the URL must be a relative path within the application.
    if (url == null || url.trim().isEmpty() || !url.startsWith("/") || url.contains("://")) {
      // Redirect to a safe default page if the URL is not valid or relative
      // This prevents redirection to arbitrary external sites.
      return new ModelAndView("redirect:/welcome.mvc"); // Example safe default
    }
    // Further validation could involve checking against a list of allowed internal paths
    // or using a URL parser to ensure it's not an external domain.
    return new ModelAndView("redirect:" + url);
  }
}
