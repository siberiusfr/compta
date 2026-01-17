package tn.cyberious.compta.auth.service;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import tn.cyberious.compta.auth.dto.AuthLogResponse;
import tn.cyberious.compta.auth.generated.tables.pojos.AuthLogs;
import tn.cyberious.compta.auth.repository.AuthLogRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthLogService {

  private final AuthLogRepository authLogRepository;

  public List<AuthLogResponse> getAllLogs(Integer limit) {
    log.debug("Getting all auth logs with limit: {}", limit);
    List<AuthLogs> logs = authLogRepository.findAll(limit);
    return logs.stream().map(this::toResponse).collect(Collectors.toList());
  }

  public List<AuthLogResponse> getUserLogs(Long userId, Integer limit) {
    log.debug("Getting auth logs for user {} with limit: {}", userId, limit);
    List<AuthLogs> logs = authLogRepository.findByUserId(userId, limit);
    return logs.stream().map(this::toResponse).collect(Collectors.toList());
  }

  public List<AuthLogResponse> getLogsByAction(String action, Integer limit) {
    log.debug("Getting auth logs for action {} with limit: {}", action, limit);
    List<AuthLogs> logs = authLogRepository.findByAction(action, limit);
    return logs.stream().map(this::toResponse).collect(Collectors.toList());
  }

  private AuthLogResponse toResponse(AuthLogs log) {
    return AuthLogResponse.builder()
        .id(log.getId())
        .userId(log.getUserId())
        .username(log.getUsername())
        .action(log.getAction())
        .ipAddress(log.getIpAddress())
        .userAgent(log.getUserAgent())
        .details(log.getDetails())
        .createdAt(log.getCreatedAt())
        .build();
  }
}
