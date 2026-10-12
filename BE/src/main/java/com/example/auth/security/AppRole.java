package com.example.auth.security;

import java.util.Arrays;
import java.util.Set;

/** System roles used by authentication, account administration, and authorization. */
public enum AppRole {
    ADMIN("ROLE_ADMIN"),
    RECRUITER("ROLE_RECRUITER"),
    HIRING_MANAGER("ROLE_HIRING_MANAGER"),
    INTERVIEWER("ROLE_INTERVIEWER"),
    HR_MANAGER("ROLE_HR_MANAGER"),
    APPROVER("ROLE_APPROVER");

    private final String authority;
    AppRole(String authority) { this.authority = authority; }
    public String authority() { return authority; }

    public static Set<String> internalAuthorities() {
        return Arrays.stream(values()).map(AppRole::authority).collect(java.util.stream.Collectors.toUnmodifiableSet());
    }

    public static boolean isInternal(String authority) { return internalAuthorities().contains(authority); }
}
