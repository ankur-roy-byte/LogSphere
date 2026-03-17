package com.ankur.loganalyzer.repository;

import com.ankur.loganalyzer.model.AlertRule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRuleRepository extends JpaRepository<AlertRule, Long> {

    List<AlertRule> findByEnabledTrue();

    List<AlertRule> findByConditionType(AlertRule.ConditionType conditionType);
}
