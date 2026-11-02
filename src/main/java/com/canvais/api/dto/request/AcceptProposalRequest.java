package com.canvais.api.dto.request;

import java.util.ArrayList;
import java.util.List;

/**
 * Request payload for accepting and committing an AI-synthesized proposal to a layer.
 */
public class AcceptProposalRequest {

    private List<String> selectedTempIds = new ArrayList<>();

    public List<String> getSelectedTempIds() {
        return selectedTempIds;
    }

    public void setSelectedTempIds(List<String> selectedTempIds) {
        this.selectedTempIds = selectedTempIds;
    }
}
