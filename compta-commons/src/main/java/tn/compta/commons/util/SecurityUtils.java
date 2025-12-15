package tn.compta.commons.util;

import jakarta.servlet.http.HttpServletRequest;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class SecurityUtils {

    private static final String HEADER_USER_ID = "X-User-Id";
    private static final String HEADER_USER_EMAIL = "X-User-Email";
    private static final String HEADER_USER_ROLES = "X-User-Roles";
    private static final String HEADER_USER_PERMISSIONS = "X-User-Permissions";
    private static final String HEADER_COMPANY_ID = "X-Company-Id";

    public static Long getUserId(HttpServletRequest request) {
        String userId = request.getHeader(HEADER_USER_ID);
        if (userId == null || userId.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(userId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static String getUserEmail(HttpServletRequest request) {
        return request.getHeader(HEADER_USER_EMAIL);
    }

    public static List<String> getUserRoles(HttpServletRequest request) {
        String roles = request.getHeader(HEADER_USER_ROLES);
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(roles.split(","));
    }

    public static List<String> getUserPermissions(HttpServletRequest request) {
        String permissions = request.getHeader(HEADER_USER_PERMISSIONS);
        if (permissions == null || permissions.isEmpty()) {
            return Collections.emptyList();
        }
        return Arrays.asList(permissions.split(","));
    }

    public static Long getCompanyId(HttpServletRequest request) {
        String companyId = request.getHeader(HEADER_COMPANY_ID);
        if (companyId == null || companyId.isEmpty()) {
            return null;
        }
        try {
            return Long.parseLong(companyId);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public static boolean hasRole(HttpServletRequest request, String role) {
        return getUserRoles(request).contains(role);
    }

    public static boolean hasAnyRole(HttpServletRequest request, String... roles) {
        List<String> userRoles = getUserRoles(request);
        return Arrays.stream(roles).anyMatch(userRoles::contains);
    }

    public static boolean hasPermission(HttpServletRequest request, String permission) {
        return getUserPermissions(request).contains(permission);
    }

    public static boolean hasAnyPermission(HttpServletRequest request, String... permissions) {
        List<String> userPermissions = getUserPermissions(request);
        return Arrays.stream(permissions).anyMatch(userPermissions::contains);
    }

    public static boolean hasAllPermissions(HttpServletRequest request, String... permissions) {
        List<String> userPermissions = getUserPermissions(request);
        return Arrays.stream(permissions).allMatch(userPermissions::contains);
    }
}
