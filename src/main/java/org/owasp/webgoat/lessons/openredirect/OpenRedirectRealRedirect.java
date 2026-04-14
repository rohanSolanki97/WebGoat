/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.openredirect;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

/**
 * Provides a real 302 redirect for experimentation separate from assignment scoring.
 */
@Controller
public class OpenRedirectRealRedirect {

  @GetMapping("/OpenRedirect/realRedirect")
  public ModelAndView real(@RequestParam("url") String url) {
    // Validate the 'url' parameter to prevent open redirects.
    // Only allow relative paths within the application or specific whitelisted domains.
    // For this lesson, we'll enforce that the URL must start with '/' for internal redirects.
    // If not, redirect to a safe default page or return an error.
    if (url != null && url.startsWith("/")) {
      // Ensure the path is normalized to prevent directory traversal within the application
      // Although Spring's redirect: handles some normalization, explicit check is safer.
      // For a simple lesson, a startsWith("/") check is a basic mitigation.
      // More robust solutions would involve a whitelist of allowed paths or a URL validator.
      return new ModelAndView("redirect:" + url);
    } else {
      // Redirect to a safe default page or error page if the URL is not valid.
      // For WebGoat, redirecting back to the lesson or a safe welcome page is appropriate.
      return new ModelAndView(new RedirectView("/welcome.mvc", true)); // Redirect to a safe internal page
    }
  }
}
