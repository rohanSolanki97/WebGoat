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
    // Fixed: Validate redirect URL to prevent Open Redirect vulnerability
    if (url == null || url.trim().isEmpty() || !url.startsWith("/") || url.contains("//")) {
      // Redirect to a safe default page or error page if the URL is invalid or external
      return new ModelAndView("redirect:/home"); // Redirect to a safe internal path
    }
    // Intentionally vulnerable: no validation
    // return new ModelAndView("redirect:" + url);
    return new ModelAndView("redirect:" + url);
  }
}
