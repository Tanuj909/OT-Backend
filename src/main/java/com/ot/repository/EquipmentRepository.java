package com.ot.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.ot.entity.Equipment;

public interface EquipmentRepository extends JpaRepository<Equipment, Long> {
    boolean existsByAssetCode(String assetCode);
    List<Equipment> findAllByHospitalId(Long hospitalId);
}
