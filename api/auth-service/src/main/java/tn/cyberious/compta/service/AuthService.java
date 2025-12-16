package tn.cyberious.compta.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jooq.DSLContext;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.auth.generated.tables.records.AuthLogsRecord;
import tn.cyberious.compta.auth.generated.tables.records.RefreshTokensRecord;
import tn.cyberious.compta.dto.AuthResponse;
import tn.cyberious.compta.dto.LoginRequest;
import tn.cyberious.compta.enums.Role;
import tn.cyberious.compta.security.CustomUserDetails;
import tn.cyberious.compta.util.JwtTokenUtil;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

import static tn.cyberious.compta.auth.generated.Tables.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final DSLContext dsl;

    @Transactional
    public AuthResponse login(LoginRequest loginRequest, String ipAddress, String userAgent) {
        log.info("Login attempt for user: {}", loginRequest.getUsername());

        try {
            // Authenticate user
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            loginRequest.getUsername(),
                            loginRequest.getPassword()
                    )
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);
            CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();

            // Generate tokens
            String accessToken = jwtTokenUtil.generateToken(userDetails, userDetails.getId());
            String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);

            // Save refresh token
            saveRefreshToken(userDetails.getId(), refreshToken, ipAddress, userAgent);

            // Update last login
            dsl.update(USERS)
                    .set(USERS.LAST_LOGIN_AT, LocalDateTime.now())
                    .set(USERS.FAILED_LOGIN_ATTEMPTS, 0)
                    .where(USERS.ID.eq(userDetails.getId()))
                    .execute();

            // Log successful login
            logAuthEvent(userDetails.getId(), loginRequest.getUsername(), "LOGIN_SUCCESS", ipAddress, userAgent, null);

            log.info("User {} logged in successfully", loginRequest.getUsername());

            return AuthResponse.builder()
                    .token(accessToken)
                    .refreshToken(refreshToken)
                    .userId(userDetails.getId())
                    .username(userDetails.getUsername())
                    .email(userDetails.getEmail())
                    .roles(userDetails.getRoleNames())
                    .build();

        } catch (Exception e) {
            log.error("Login failed for user: {}", loginRequest.getUsername(), e);

            // Increment failed login attempts
            var userRecord = dsl.selectFrom(USERS)
                    .where(USERS.USERNAME.eq(loginRequest.getUsername()))
                    .fetchOne();

            if (userRecord != null) {
                int attempts = userRecord.getFailedLoginAttempts() != null ? userRecord.getFailedLoginAttempts() : 0;
                attempts++;

                dsl.update(USERS)
                        .set(USERS.FAILED_LOGIN_ATTEMPTS, attempts)
                        .where(USERS.ID.eq(userRecord.getId()))
                        .execute();

                // Lock account after 5 failed attempts
                if (attempts >= 5) {
                    dsl.update(USERS)
                            .set(USERS.IS_LOCKED, true)
                            .where(USERS.ID.eq(userRecord.getId()))
                            .execute();
                    log.warn("Account locked for user: {} after {} failed attempts", loginRequest.getUsername(), attempts);
                }

                logAuthEvent(userRecord.getId(), loginRequest.getUsername(), "LOGIN_FAILED", ipAddress, userAgent, e.getMessage());
            } else {
                logAuthEvent(null, loginRequest.getUsername(), "LOGIN_FAILED", ipAddress, userAgent, "User not found");
            }

            throw new RuntimeException("Invalid username or password");
        }
    }

    @Transactional
    public AuthResponse refreshToken(String refreshToken) {
        log.info("Refreshing token");

        try {
            String username = jwtTokenUtil.getUsernameFromToken(refreshToken);

            // Verify refresh token exists in database
            var tokenRecord = dsl.selectFrom(REFRESH_TOKENS)
                    .where(REFRESH_TOKENS.TOKEN.eq(refreshToken))
                    .fetchOne();

            if (tokenRecord == null || tokenRecord.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.error("Refresh token invalid or expired");
                throw new RuntimeException("Invalid or expired refresh token");
            }

            // Load user
            var userRecord = dsl.selectFrom(USERS)
                    .where(USERS.ID.eq(tokenRecord.getUserId()))
                    .fetchOne();

            if (userRecord == null) {
                throw new RuntimeException("User not found");
            }

            // Get user roles
            var roles = dsl.select(ROLES.NAME)
                    .from(USER_ROLES)
                    .join(ROLES).on(USER_ROLES.ROLE_ID.eq(ROLES.ID))
                    .where(USER_ROLES.USER_ID.eq(userRecord.getId()))
                    .fetch(ROLES.NAME)
                    .stream()
                    .map(Role::fromName)
                    .collect(Collectors.toList());

            CustomUserDetails userDetails = new CustomUserDetails(
                    userRecord.getId(),
                    userRecord.getUsername(),
                    userRecord.getEmail(),
                    userRecord.getPassword(),
                    userRecord.getIsActive(),
                    userRecord.getIsLocked(),
                    roles
            );

            // Generate new access token
            String newAccessToken = jwtTokenUtil.generateToken(userDetails, userDetails.getId());

            return AuthResponse.builder()
                    .token(newAccessToken)
                    .refreshToken(refreshToken)
                    .userId(userDetails.getId())
                    .username(userDetails.getUsername())
                    .email(userDetails.getEmail())
                    .roles(userDetails.getRoleNames())
                    .build();

        } catch (Exception e) {
            log.error("Token refresh failed", e);
            throw new RuntimeException("Token refresh failed: " + e.getMessage());
        }
    }

    private void saveRefreshToken(Long userId, String token, String ipAddress, String userAgent) {
        // Delete old refresh tokens for this user
        dsl.deleteFrom(REFRESH_TOKENS)
                .where(REFRESH_TOKENS.USER_ID.eq(userId))
                .execute();

        // Save new refresh token
        RefreshTokensRecord record = dsl.newRecord(REFRESH_TOKENS);
        record.setUserId(userId);
        record.setToken(token);
        record.setExpiresAt(LocalDateTime.now().plusDays(7));
        record.setIpAddress(ipAddress);
        record.setUserAgent(userAgent);
        record.store();
    }

    private void logAuthEvent(Long userId, String username, String action, String ipAddress, String userAgent, String details) {
        AuthLogsRecord log = dsl.newRecord(AUTH_LOGS);
        log.setUserId(userId);
        log.setUsername(username);
        log.setAction(action);
        log.setIpAddress(ipAddress);
        log.setUserAgent(userAgent);
        log.setDetails(details);
        log.store();
    }
}
