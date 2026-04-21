package com.ot.repository;

import com.ot.entity.WardTask;
import com.ot.enums.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface WardTaskRepository extends JpaRepository<WardTask, Long> {

    // Operation ke saare tasks
    List<WardTask> findByScheduledOperationIdOrderByScheduledTimeAsc(Long operationId);

    // WardAdmission ke saare tasks
    List<WardTask> findByWardAdmissionIdOrderByScheduledTimeAsc(Long wardAdmissionId);

    // Status filter ke saath — e.g. sirf PENDING tasks
    List<WardTask> findByScheduledOperationIdAndStatusOrderByScheduledTimeAsc(
            Long operationId, TaskStatus status);

    // Patient ke saare tasks (history)
    List<WardTask> findByPatientIdOrderByCreatedAtDesc(String patientId);

    // Hospital level — saare pending tasks
    List<WardTask> findByHospitalIdAndStatusOrderByScheduledTimeAsc(
            Long hospitalId, TaskStatus status);
}