package com.catprogrammer.cogedimscanner.controller;

import com.catprogrammer.cogedimscanner.service.AuthService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.security.Principal;

@RestController
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }


    /**
     * 登录
     */
    @PostMapping(value = "/auth/login")
    public String login(@RequestParam String username, @RequestParam String password) throws AuthenticationException {
        // 登录成功会返回Token给用户
        return authService.login(username, password);
    }

    @PreAuthorize("hasAuthority('READ_PRIVILEGE')")
    @PostMapping(value = "/user/hi")
    public String userHi(Principal principal) throws AuthenticationException {
        return "hi " + principal.getName() + " , you have 'user' role";
    }

    @PreAuthorize("hasAuthority('WRITE_PRIVILEGE')")
    @PostMapping(value = "/admin/hi")
    public String adminHi(Principal principal) throws AuthenticationException {
        return "{\"user\": \"" + principal.getName() + "\", \"role\": \"admin\"}";
    }
}
