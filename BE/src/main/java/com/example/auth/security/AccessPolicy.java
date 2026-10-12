package com.example.auth.security;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.Map;
import java.util.Set;

/** Central role-to-permission map. Controllers enforce these permissions at the API boundary. */
@Component("accessPolicy")
public class AccessPolicy {
    private static final Map<Permission, Set<AppRole>> GRANTS = grants();

    public boolean isInternal(Authentication authentication) {
        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(a -> AppRole.isInternal(a.getAuthority()));
    }

    public boolean has(Authentication authentication, String permission) {
        if (authentication == null || !authentication.isAuthenticated()) return false;
        Permission key;
        try { key = Permission.valueOf(permission); }
        catch (IllegalArgumentException ex) { return false; }
        Set<AppRole> grantedRoles = GRANTS.getOrDefault(key, Set.of());
        return grantedRoles.stream().anyMatch(role -> authentication.getAuthorities().stream()
                .anyMatch(authority -> role.authority().equals(authority.getAuthority())));
    }

    private static Map<Permission, Set<AppRole>> grants() {
        EnumMap<Permission, Set<AppRole>> map = new EnumMap<>(Permission.class);
        map.put(Permission.MANAGE_ACCOUNTS, Set.of(AppRole.ADMIN));
        Set<AppRole> allInternalRoles = Set.copyOf(java.util.EnumSet.allOf(AppRole.class));
        map.put(Permission.VIEW_JOBS, allInternalRoles);
        map.put(Permission.APPLY_FOR_JOB, Set.of(AppRole.ADMIN, AppRole.HR_MANAGER, AppRole.RECRUITER, AppRole.HIRING_MANAGER, AppRole.INTERVIEWER));
        map.put(Permission.MANAGE_JOBS, Set.of(AppRole.ADMIN, AppRole.HR_MANAGER, AppRole.RECRUITER, AppRole.HIRING_MANAGER));
        map.put(Permission.VIEW_CANDIDATES, Set.of(AppRole.ADMIN, AppRole.HR_MANAGER, AppRole.RECRUITER, AppRole.HIRING_MANAGER, AppRole.INTERVIEWER));
        map.put(Permission.MANAGE_CANDIDATES, Set.of(AppRole.ADMIN, AppRole.HR_MANAGER, AppRole.RECRUITER));
        map.put(Permission.VIEW_APPLICATIONS, Set.of(AppRole.ADMIN, AppRole.HR_MANAGER, AppRole.RECRUITER, AppRole.HIRING_MANAGER, AppRole.INTERVIEWER));
        map.put(Permission.CREATE_APPLICATION, Set.of(AppRole.ADMIN, AppRole.HR_MANAGER, AppRole.RECRUITER));
        map.put(Permission.MANAGE_PIPELINE, Set.of(AppRole.ADMIN, AppRole.HR_MANAGER, AppRole.RECRUITER, AppRole.HIRING_MANAGER));
        map.put(Permission.SCHEDULE_INTERVIEWS, Set.of(AppRole.ADMIN, AppRole.HR_MANAGER, AppRole.RECRUITER));
        map.put(Permission.MANAGE_ONBOARDING, Set.of(AppRole.ADMIN, AppRole.HR_MANAGER));
        map.put(Permission.VIEW_EVALUATIONS, Set.of(AppRole.ADMIN, AppRole.HR_MANAGER, AppRole.RECRUITER, AppRole.HIRING_MANAGER, AppRole.INTERVIEWER));
        map.put(Permission.SUBMIT_EVALUATIONS, Set.of(AppRole.ADMIN, AppRole.HR_MANAGER, AppRole.HIRING_MANAGER, AppRole.INTERVIEWER));
        map.put(Permission.APPROVE_OFFERS, Set.of(AppRole.ADMIN, AppRole.HR_MANAGER, AppRole.APPROVER));
        map.put(Permission.VIEW_REPORTS, Set.of(AppRole.ADMIN, AppRole.HR_MANAGER));
        map.put(Permission.MANAGE_CATALOGS, Set.of(AppRole.ADMIN, AppRole.HR_MANAGER));
        map.put(Permission.VIEW_REQUISITIONS, Set.of(AppRole.ADMIN, AppRole.HR_MANAGER, AppRole.HIRING_MANAGER, AppRole.APPROVER, AppRole.RECRUITER));
        map.put(Permission.MANAGE_REQUISITIONS, Set.of(AppRole.ADMIN, AppRole.HR_MANAGER, AppRole.HIRING_MANAGER));
        map.put(Permission.APPROVE_REQUISITIONS, Set.of(AppRole.ADMIN, AppRole.HR_MANAGER, AppRole.APPROVER));
        map.put(Permission.MANAGE_NOTIFICATIONS, Set.of(AppRole.ADMIN, AppRole.HR_MANAGER, AppRole.RECRUITER));
        return Map.copyOf(map);
    }

    private enum Permission {
        MANAGE_ACCOUNTS, VIEW_JOBS, APPLY_FOR_JOB, MANAGE_JOBS, VIEW_CANDIDATES, MANAGE_CANDIDATES,
        VIEW_APPLICATIONS, CREATE_APPLICATION, MANAGE_PIPELINE, SCHEDULE_INTERVIEWS, MANAGE_ONBOARDING,
        VIEW_EVALUATIONS, SUBMIT_EVALUATIONS, APPROVE_OFFERS, VIEW_REPORTS,
        MANAGE_CATALOGS, VIEW_REQUISITIONS, MANAGE_REQUISITIONS, APPROVE_REQUISITIONS, MANAGE_NOTIFICATIONS
    }
}
