package com.ot.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ot.entity.AnesthesiaDrug;
import com.ot.entity.IntraOpRecord;
import com.ot.enums.DrugType;

public interface AnesthesiaDrugRepository extends JpaRepository<AnesthesiaDrug, Long> {
    List<AnesthesiaDrug> findByIntraOp(IntraOpRecord intraOp);
    List<AnesthesiaDrug> findByIntraOpAndDrugType(IntraOpRecord intraOp, DrugType drugType);
}