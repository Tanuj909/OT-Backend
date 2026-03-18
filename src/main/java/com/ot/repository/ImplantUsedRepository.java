package com.ot.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ot.entity.ImplantUsed;
import com.ot.entity.OTItemCatalog;
import com.ot.entity.ScheduledOperation;

public interface ImplantUsedRepository extends JpaRepository<ImplantUsed, Long> {
    List<ImplantUsed> findByScheduledOperation(ScheduledOperation operation);
    boolean existsByScheduledOperationAndCatalogItem(ScheduledOperation operation, OTItemCatalog catalogItem);
}