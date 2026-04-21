package com.ot.service;

import com.ot.dto.ward.CreateWardTaskRequest;
import com.ot.dto.ward.CompleteWardTaskRequest;
import com.ot.dto.ward.WardTaskResponse;
import com.ot.enums.TaskStatus;
import java.util.List;

public interface WardTaskService {

    WardTaskResponse createTask(CreateWardTaskRequest request);

    WardTaskResponse completeTask(Long taskId, CompleteWardTaskRequest request);

    WardTaskResponse cancelTask(Long taskId);

    WardTaskResponse getById(Long taskId);

    List<WardTaskResponse> getByOperation(Long operationId);

    List<WardTaskResponse> getByAdmission(Long wardAdmissionId);

    List<WardTaskResponse> getByOperationAndStatus(Long operationId, TaskStatus status);
}