/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.openredirect;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView; // Added import

/**
 * Provides a real 302 redirect for experimentation separate from assignment scoring.
 */
@Controller
public class OpenRedirectRealRedirect {

  @GetMapping("/OpenRedirect/realRedirect")
  public ModelAndView real(@RequestParam("url") String url) {
    // Remediation: Validate redirect target to prevent Open Redirect
    // Ensure the URL is not null, not empty, starts with '/', and does not contain scheme-relative '//' or backslashes.
    if (url == null || url.trim().isEmpty() || !url.startsWith("/") || url.contains("//") || url.contains("\\")) {
      // Redirect to a safe default page or return an error if validation fails
      return new ModelAndView("redirect:/"); // Redirect to home page as a safe default
    }
    return new ModelAndView("redirect:" + url);
  }
}
