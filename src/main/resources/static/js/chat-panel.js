// CANVAiS Local AI Chat & Layer Sketching Panel Controller
const ChatPanelClient = {
    chatSessionId: null,
    targetOverlayLayerId: null,
    overlayGeoJsonLayer: null,
    isOpen: true,

    init() {
        this.bindEvents();
        this.checkHealth();
    },

    bindEvents() {
        const btnToggle = document.getElementById('btn-toggle-chat');
        const btnClose = document.getElementById('btn-close-chat');
        const chatPanel = document.getElementById('chat-panel');
        const chatForm = document.getElementById('chat-form');
        const btnClear = document.getElementById('btn-clear-chat-layer');
        const btnForwardVibe = document.getElementById('btn-chat-vibe-forward');

        if (btnToggle) {
            btnToggle.addEventListener('click', () => {
                this.isOpen = !this.isOpen;
                chatPanel.classList.toggle('collapsed', !this.isOpen);
                btnToggle.classList.toggle('active', this.isOpen);
            });
        }

        if (btnClose) {
            btnClose.addEventListener('click', () => {
                this.isOpen = false;
                chatPanel.classList.add('collapsed');
                if (btnToggle) btnToggle.classList.remove('active');
            });
        }

        if (chatForm) {
            chatForm.addEventListener('submit', (e) => {
                e.preventDefault();
                this.handlePromptSubmit();
            });
        }

        if (btnClear) {
            btnClear.addEventListener('click', () => {
                this.clearOverlayLayer();
            });
        }

        if (btnForwardVibe) {
            btnForwardVibe.addEventListener('click', () => {
                this.forwardActiveVibe();
            });
        }
    },

    async checkHealth() {
        const dot = document.getElementById('ai-health-status');
        const badge = document.getElementById('chat-model-badge');
        try {
            const res = await fetch('/api/v1/chat/health');
            const data = await res.json();
            if (dot) {
                dot.className = 'status-dot';
                if (data.status === 'AVAILABLE') {
                    dot.classList.add('connected');
                    dot.title = `Connected (${data.activeModel}) - ${data.latencyMs}ms`;
                } else if (data.status === 'BUSY' || data.status === 'DEGRADED') {
                    dot.classList.add('degraded');
                    dot.title = `Model Degraded / Busy: ${data.message}`;
                } else {
                    dot.classList.add('offline');
                    dot.title = `Ollama Offline: ${data.message}`;
                }
            }
            if (badge && data.activeModel) {
                badge.textContent = `Ollama (${data.activeModel})`;
            }
        } catch (err) {
            if (dot) {
                dot.className = 'status-dot offline';
                dot.title = "Local AI service unreachable";
            }
        }
    },

    async ensureSession(mapSessionId) {
        if (!mapSessionId) return;
        try {
            const res = await fetch('/api/v1/chat/sessions', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    mapSessionId: mapSessionId,
                    title: "Tactical Chat Session"
                })
            });
            if (res.ok) {
                const session = await res.json();
                this.chatSessionId = session.id;
                this.targetOverlayLayerId = session.targetLayerId;
                const statusEl = document.getElementById('chat-overlay-status');
                if (statusEl) {
                    statusEl.textContent = "Overlay: Active";
                }
                await this.loadSessionHistory();
                await this.refreshOverlayVisuals();
            }
        } catch (err) {
            console.error("Failed to initialize chat session:", err);
        }
    },

    async loadSessionHistory() {
        if (!this.chatSessionId) return;
        try {
            const res = await fetch(`/api/v1/chat/sessions/${this.chatSessionId}`);
            if (res.ok) {
                const data = await res.json();
                const container = document.getElementById('chat-messages');
                if (!container) return;
                container.innerHTML = '';
                if (data.messages && data.messages.length > 0) {
                    data.messages.forEach(msg => this.appendMessageBubble(msg.role, msg.content, msg.actionType));
                } else {
                    container.innerHTML = `
                        <div class="chat-bubble system">
                            <p>👋 Chat initialized. Describe geographical objects to draw onto the dedicated overlay layer, or forward vibe voice records.</p>
                        </div>
                    `;
                }
            }
        } catch (err) {
            console.error("Failed to load chat history:", err);
        }
    },

    async handlePromptSubmit() {
        const input = document.getElementById('chat-prompt-input');
        const text = input ? input.value.trim() : '';
        if (!text) return;

        if (!this.chatSessionId && CanvaisApp && CanvaisApp.currentSessionId) {
            await this.ensureSession(CanvaisApp.currentSessionId);
        }
        if (!this.chatSessionId) {
            alert("Please create or select a map session first.");
            return;
        }

        this.appendMessageBubble('USER', text);
        input.value = '';

        const bounds = CanvaisApp.map ? CanvaisApp.map.getBounds() : null;
        const center = CanvaisApp.map ? CanvaisApp.map.getCenter() : null;

        const payload = {
            message: text,
            viewportBounds: bounds ? {
                north: bounds.getNorth(),
                south: bounds.getSouth(),
                east: bounds.getEast(),
                west: bounds.getWest()
            } : null,
            centerCoordinates: center ? {
                lat: center.lat,
                lng: center.lng
            } : null
        };

        try {
            const res = await fetch(`/api/v1/chat/sessions/${this.chatSessionId}/messages`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });

            if (res.ok) {
                const turn = await res.json();
                this.appendMessageBubble('ASSISTANT', turn.assistantMessage.content, turn.actionType, turn.affectedEntities);
                await this.refreshOverlayVisuals();
            } else {
                this.appendMessageBubble('SYSTEM', "Error communicating with the local AI assistant. Please check service status.");
            }
        } catch (err) {
            console.error("Send message error:", err);
            this.appendMessageBubble('SYSTEM', "Failed to dispatch message to local model: " + err.message);
        }
    },

    async forwardActiveVibe() {
        const textInput = document.getElementById('vibe-text-input');
        const vibeText = textInput ? textInput.value.trim() : '';
        if (!vibeText) {
            const promptVal = prompt("Enter vibe transcript or description to send to Chat:", "Draw an evacuation route north");
            if (!promptVal) return;
            textInput.value = promptVal;
        }

        const bounds = CanvaisApp.map ? CanvaisApp.map.getBounds() : null;
        const payload = {
            vibeTranscript: textInput.value.trim(),
            viewportBounds: bounds ? {
                north: bounds.getNorth(),
                south: bounds.getSouth(),
                east: bounds.getEast(),
                west: bounds.getWest()
            } : null
        };

        try {
            const res = await fetch(`/api/v1/chat/sessions/${this.chatSessionId}/vibe`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            if (res.ok) {
                const turn = await res.json();
                this.appendMessageBubble('USER', `🎙️ Vibe: ${turn.userMessage.content}`);
                this.appendMessageBubble('ASSISTANT', turn.assistantMessage.content, turn.actionType, turn.affectedEntities);
                await this.refreshOverlayVisuals();
            }
        } catch (err) {
            console.error("Forward vibe to chat failed:", err);
        }
    },

    async clearOverlayLayer() {
        if (!this.chatSessionId) return;
        if (!confirm("Clear all AI-sketched objects from the overlay layer?")) return;

        try {
            const res = await fetch(`/api/v1/chat/sessions/${this.chatSessionId}/layer`, {
                method: 'DELETE'
            });
            if (res.ok) {
                const data = await res.json();
                this.appendMessageBubble('SYSTEM', `Overlay layer cleared (${data.clearedEntitiesCount} entities removed).`);
                if (this.overlayGeoJsonLayer && CanvaisApp && CanvaisApp.map) {
                    CanvaisApp.map.removeLayer(this.overlayGeoJsonLayer);
                    this.overlayGeoJsonLayer = null;
                }
            }
        } catch (err) {
            console.error("Failed to clear overlay layer:", err);
        }
    },

    async refreshOverlayVisuals() {
        if (!this.targetOverlayLayerId || !CanvaisApp || !CanvaisApp.map) return;
        try {
            const res = await fetch(`/api/v1/layers/${this.targetOverlayLayerId}/entities`);
            if (res.ok) {
                const geoJson = await res.json();
                if (this.overlayGeoJsonLayer) {
                    CanvaisApp.map.removeLayer(this.overlayGeoJsonLayer);
                }
                this.overlayGeoJsonLayer = L.geoJSON(geoJson, {
                    style: (feature) => ({
                        color: feature.properties.strokeColor || '#E76F51',
                        fillColor: feature.properties.fillColor || '#E76F5133',
                        weight: feature.properties.strokeWidth || 2.5
                    }),
                    onEachFeature: (feature, layer) => {
                        if (feature.properties && feature.properties.name) {
                            layer.bindPopup(`<strong>${feature.properties.name}</strong><br><em>Overlay Object</em>`);
                        }
                    }
                }).addTo(CanvaisApp.map);
            }
        } catch (err) {
            console.error("Failed to refresh overlay visuals:", err);
        }
    },

    appendMessageBubble(role, content, actionType = 'NONE', entities = []) {
        const container = document.getElementById('chat-messages');
        if (!container) return;

        const bubble = document.createElement('div');
        const roleClass = (role || 'assistant').toLowerCase();
        bubble.className = `chat-bubble ${roleClass}`;

        let html = `<p>${content}</p>`;
        if (entities && entities.length > 0) {
            html += `<div class="chat-card-preview"><strong>⚡ Action: ${actionType}</strong><br>`;
            entities.forEach(ent => {
                html += `<span>• ${ent.name} (${ent.entityType || 'Vector'})</span><br>`;
            });
            html += `</div>`;
        }

        bubble.innerHTML = html;
        container.appendChild(bubble);
        container.scrollTop = container.scrollHeight;
    }
};

window.addEventListener('DOMContentLoaded', () => {
    ChatPanelClient.init();
});
