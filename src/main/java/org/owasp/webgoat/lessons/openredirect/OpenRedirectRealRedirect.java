/*
 * SPDX-FileCopyrightText: Copyright © 2025 WebGoat authors
 * SPDX-License-Identifier: GPL-2.0-or-later
 */
package org.owasp.webgoat.lessons.openredirect;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.util.StringUtils; // Import StringUtils

/**
 * Provides a real 302 redirect for experimentation separate from assignment scoring.
 */
@Controller
public class OpenRedirectRealRedirect {

  @GetMapping("/OpenRedirect/realRedirect")
  public ModelAndView real(@RequestParam("url") String url) {
    // Validate redirect target against a whitelist or ensure it's an internal path
    if (StringUtils.hasText(url) && url.startsWith("/")) {
      return new ModelAndView("redirect:" + url);
    } else {
      // Default to a safe internal page or an error page if validation fails
      return new ModelAndView("redirect:/welcome.mvc"); // Redirect to a safe default page
    }
  }
}
