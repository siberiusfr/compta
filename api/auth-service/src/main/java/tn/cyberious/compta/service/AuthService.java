package tn.cyberious.compta.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.cyberious.compta.auth.generated.tables.pojos.RefreshTokens;
import tn.cyberious.compta.auth.generated.tables.pojos.Users;
import tn.cyberious.compta.dto.AuthResponse;
import tn.cyberious.compta.dto.LoginRequest;
import tn.cyberious.compta.enums.Role;
import tn.cyberious.compta.repository.AuthLogRepository;
import tn.cyberious.compta.repository.RefreshTokenRepository;
import tn.cyberious.compta.repository.UserRepository;
import tn.cyberious.compta.security.CustomUserDetails;
import tn.cyberious.compta.util.JwtTokenUtil;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenUtil jwtTokenUtil;
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final AuthLogRepository authLogRepository;

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
            Users user = userRepository.findById(userDetails.getId())
                    .orElseThrow(() -> new RuntimeException("User not found"));
            user.setLastLoginAt(LocalDateTime.now());
            user.setFailedLoginAttempts(0);
            userRepository.update(user);

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
            var userOpt = userRepository.findByUsername(loginRequest.getUsername());

            if (userOpt.isPresent()) {
                Users user = userOpt.get();
                int attempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0;
                attempts++;

                user.setFailedLoginAttempts(attempts);

                // Lock account after 5 failed attempts
                if (attempts >= 5) {
                    user.setIsLocked(true);
                    log.warn("Account locked for user: {} after {} failed attempts", loginRequest.getUsername(), attempts);
                }

                userRepository.update(user);

                logAuthEvent(user.getId(), loginRequest.getUsername(), "LOGIN_FAILED", ipAddress, userAgent, e.getMessage());
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
            RefreshTokens token = refreshTokenRepository.findByToken(refreshToken)
                    .orElseThrow(() -> new RuntimeException("Invalid or expired refresh token"));

            if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
                log.error("Refresh token expired");
                throw new RuntimeException("Invalid or expired refresh token");
            }

            // Load user
            Users user = userRepository.findById(token.getUserId())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Get user roles
            List<Role> roles = userRepository.findRolesByUserId(user.getId());

            CustomUserDetails userDetails = new CustomUserDetails(
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getPassword(),
                    user.getIsActive(),
                    user.getIsLocked(),
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
        refreshTokenRepository.deleteByUserId(userId);

        // Save new refresh token
        RefreshTokens refreshToken = new RefreshTokens();
        refreshToken.setUserId(userId);
        refreshToken.setToken(token);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshToken.setIpAddress(ipAddress);
        refreshToken.setUserAgent(userAgent);
        refreshTokenRepository.insert(refreshToken);
    }

    private void logAuthEvent(Long userId, String username, String action, String ipAddress, String userAgent, String details) {
        authLogRepository.log(userId, username, action, ipAddress, userAgent, details);
    }
}
