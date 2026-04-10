/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.openredirect;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView; // Remediation: Added import for RedirectView

/**
 * Provides a real 302 redirect for experimentation separate from assignment scoring.
 */
@Controller
public class OpenRedirectRealRedirect {

  @GetMapping("/OpenRedirect/realRedirect")
  public ModelAndView real(@RequestParam("url") String url) {
    // Remediation: Validate the redirect URL to prevent Open Redirect
    // Only allow relative paths within the application or a specific whitelist.
    // For this lesson, we enforce relative paths starting with '/'.
    if (url != null && url.startsWith("/")) {
      return new ModelAndView("redirect:" + url);
    } else {
      // Fallback to a safe default (e.g., home page) or return an error
      return new ModelAndView("redirect:/"); // Redirect to application root
    }
  }
}
