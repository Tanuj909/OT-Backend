package com.ot.service;

import com.ot.entity.User;
import com.ot.enums.ActionType;

public interface AuditService {
    void log(User user, ActionType action, String entityName, String entityId, String oldValue, String newValue, String description);
    void logFailure(User user, ActionType action, String entityName, String entityId, String errorMessage);
}