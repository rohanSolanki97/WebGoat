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
    // Fix: Validate that the URL starts with a '/' to ensure it's an internal path
    // and prevent redirects to external, potentially malicious, sites.
    if (url != null && url.startsWith("/")) {
      return new ModelAndView("redirect:" + url);
    } else {
      // Redirect to a safe default page if the URL is not an internal path or is null/empty
      return new ModelAndView("redirect:/"); // Redirect to root context path
    }
  }
}
