/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.openredirect;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView; // Import RedirectView

/**
 * Provides a real 302 redirect for experimentation separate from assignment scoring.
 */
@Controller
public class OpenRedirectRealRedirect {

  @GetMapping("/OpenRedirect/realRedirect")
  public ModelAndView real(@RequestParam("url") String url) {
    // Validate the URL to prevent open redirects
    // Only allow relative paths starting with '/' and prevent scheme-relative URLs (e.g., //evil.com)
    // Also, ensure it's not an absolute URL to an external domain.
    if (url != null && url.startsWith("/") && !url.startsWith("//") && !url.contains("://")) {
      return new ModelAndView("redirect:" + url); // Safe redirect to internal path
    } else {
      // Default to a safe page or error page if the URL is not valid
      return new ModelAndView("redirect:/home"); // Redirect to a safe, known internal page
    }
  }
}
