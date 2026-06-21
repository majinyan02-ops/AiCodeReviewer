package com.aicode.patch.service;

import com.aicode.fix.model.FixSuggestion;
import com.aicode.patch.model.PatchResult;

public interface PatchService {

    PatchResult generatePatch(FixSuggestion suggestion);
}
