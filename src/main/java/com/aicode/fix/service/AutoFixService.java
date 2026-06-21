package com.aicode.fix.service;

import com.aicode.fix.model.FixSuggestion;
import com.aicode.rule.model.RuleResult;

public interface AutoFixService {

    FixSuggestion generateFix(RuleResult ruleResult);
}
