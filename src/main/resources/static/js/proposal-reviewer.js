// CANVAiS Proposal Review & Preview Overlay
const ProposalReviewer = {
    currentProposal: null,
    previewLayerGroup: null,

    init() {
        this.previewLayerGroup = L.layerGroup().addTo(CanvaisApp.map);

        document.getElementById('btn-accept-proposal').addEventListener('click', () => {
            this.acceptProposal();
        });

        document.getElementById('btn-reject-proposal').addEventListener('click', () => {
            this.rejectProposal();
        });
    },

    showProposal(proposal) {
        this.currentProposal = proposal;
        document.getElementById('proposal-summary').textContent = proposal.interpretedSummary;
        document.getElementById('proposal-banner').classList.remove('hidden');

        // Clear previous previews
        this.previewLayerGroup.clearLayers();

        // Render preview entities with dash animation or highlight
        if (proposal.proposedEntities) {
            proposal.proposedEntities.forEach(entity => {
                if (entity.geometry) {
                    const preview = L.geoJSON({
                        type: "Feature",
                        geometry: entity.geometry,
                        properties: entity.semanticAttributes || {}
                    }, {
                        style: {
                            color: '#E76F51',
                            weight: 3,
                            dashArray: '6, 6',
                            fillColor: '#E76F5133'
                        }
                    });
                    this.previewLayerGroup.addLayer(preview);
                }
            });
        }
    },

    async acceptProposal() {
        if (!this.currentProposal) return;
        try {
            await fetch(`/api/v1/vibe/proposals/${this.currentProposal.proposalId}/accept`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({})
            });

            // Dismiss proposal overlay
            this.dismiss();
            GestureRecorder.clearCanvas();

            // Refresh target layer GeoJSON on map
            if (CanvaisApp.currentLayerId) {
                CanvaisApp.fetchAndRenderLayerGeoJson(CanvaisApp.currentLayerId);
            }
        } catch (err) {
            console.error("Accepting proposal failed:", err);
        }
    },

    rejectProposal() {
        this.dismiss();
        GestureRecorder.clearCanvas();
    },

    dismiss() {
        this.currentProposal = null;
        this.previewLayerGroup.clearLayers();
        document.getElementById('proposal-banner').classList.add('hidden');
    }
};

window.addEventListener('DOMContentLoaded', () => {
    ProposalReviewer.init();
});
