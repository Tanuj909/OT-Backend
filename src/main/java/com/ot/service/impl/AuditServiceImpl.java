package com.ot.service.impl;

import org.springframework.stereotype.Service;
import com.ot.entity.AuditTrail;
import com.ot.entity.User;
import com.ot.enums.ActionType;
import com.ot.enums.AuditStatus;
import com.ot.repository.AuditTrailRepository;
import com.ot.service.AuditService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuditServiceImpl implements AuditService {

    private final AuditTrailRepository auditTrailRepository;
    private final HttpServletRequest httpServletRequest;

    @Override
    public void log(User user, ActionType action, String entityName, 
                    String entityId, String oldValue, String newValue, String description) {

        AuditTrail audit = AuditTrail.builder()
                .hospital(user.getHospital())
                .userId(user.getId().toString())
                .userName(user.getUserName())
                .userRole(user.getRole().name())
                .ipAddress(getClientIp())
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .oldValue(oldValue)
                .newValue(newValue)
                .description(description)
                .status(AuditStatus.SUCCESS)
                .build();

        auditTrailRepository.save(audit);
    }

    @Override
    public void logFailure(User user, ActionType action, String entityName,
                           String entityId, String errorMessage) {

        AuditTrail audit = AuditTrail.builder()
                .hospital(user != null ? user.getHospital() : null)
                .userId(user != null ? user.getId().toString() : "UNKNOWN")
                .userName(user != null ? user.getUserName() : "UNKNOWN")
                .userRole(user != null ? user.getRole().name() : "UNKNOWN")
                .ipAddress(getClientIp())
                .action(action)
                .entityName(entityName)
                .entityId(entityId)
                .status(AuditStatus.FAILURE)
                .errorMessage(errorMessage)
                .build();

        auditTrailRepository.save(audit);
    }

    // Client IP fetch karo — proxy ke peeche bhi kaam kare
    private String getClientIp() {
        String xForwardedFor = httpServletRequest.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return httpServletRequest.getRemoteAddr();
    }
}