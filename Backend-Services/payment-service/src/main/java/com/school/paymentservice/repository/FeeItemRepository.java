package com.school.paymentservice.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.school.paymentservice.entity.FeeItem;

@Repository
public interface FeeItemRepository extends JpaRepository<FeeItem, Long> {

    List<FeeItem> findByMandatory(boolean mandatory);

    List<FeeItem> findByClassIdAndMandatoryAndActiveTrue(Long classId, boolean mandatory);

    List<FeeItem> findByClassIdAndActiveTrue(Long classId);

    List<FeeItem> findByActiveTrue();
}
