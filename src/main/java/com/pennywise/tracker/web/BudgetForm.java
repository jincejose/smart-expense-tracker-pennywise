package com.pennywise.tracker.web;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class BudgetForm {
    private Map<String, BigDecimal> limits = new LinkedHashMap<>();
    public Map<String, BigDecimal> getLimits() { return limits; }
    public void setLimits(Map<String, BigDecimal> limits) { this.limits = limits; }
}
