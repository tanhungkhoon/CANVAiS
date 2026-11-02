// CANVAiS Map Renderer & Session Controller
const CanvaisApp = {
    map: null,
    currentSessionId: null,
    currentLayerId: null,
    layersMap: new Map(), // layerId -> L.GeoJSON group
    isDrawingMode: false,

    async init() {
        this.initMap();
        this.bindEvents();
        await this.loadSessions();
    },

    initMap() {
        // Initialize Leaflet map
        this.map = L.map('map', {
            center: [48.1800, 8.2000],
            zoom: 12,
            zoomControl: true
        });

        // Add OpenStreetMap base tiles
        L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
            maxZoom: 19,
            attribution: '&copy; OpenStreetMap contributors | CANVAiS'
        }).addTo(this.map);
    },

    bindEvents() {
        document.getElementById('session-dropdown').addEventListener('change', (e) => {
            if (e.target.value) {
                this.switchSession(e.target.value);
            }
        });

        document.getElementById('btn-new-session').addEventListener('click', () => {
            this.promptNewSession();
        });

        document.getElementById('btn-add-layer').addEventListener('click', () => {
            this.promptNewLayer();
        });

        // Mode toggles
        const btnPan = document.getElementById('btn-mode-pan');
        const btnDraw = document.getElementById('btn-mode-draw');
        const gestureCanvas = document.getElementById('gesture-canvas');

        btnPan.addEventListener('click', () => {
            btnPan.classList.add('active');
            btnDraw.classList.remove('active');
            gestureCanvas.classList.remove('drawing-active');
            this.isDrawingMode = false;
        });

        btnDraw.addEventListener('click', () => {
            btnDraw.classList.add('active');
            btnPan.classList.remove('active');
            gestureCanvas.classList.add('drawing-active');
            this.isDrawingMode = true;
        });
    },

    async loadSessions() {
        try {
            const res = await fetch('/api/v1/sessions');
            const sessions = await res.json();
            const dropdown = document.getElementById('session-dropdown');
            dropdown.innerHTML = '';

            if (sessions.length === 0) {
                // Auto create a default session for initial user experience
                const created = await this.createSession("Default Operations Canvas", 48.1800, 8.2000, 12);
                sessions.push(created);
            }

            sessions.forEach(s => {
                const opt = document.createElement('option');
                opt.value = s.id;
                opt.textContent = s.title;
                dropdown.appendChild(opt);
            });

            if (sessions.length > 0) {
                this.switchSession(sessions[0].id);
            }
        } catch (err) {
            console.error("Failed to load sessions:", err);
        }
    },

    async createSession(title, lat, lng, zoom) {
        const res = await fetch('/api/v1/sessions', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                title: title,
                centerLat: lat,
                centerLng: lng,
                zoomLevel: zoom
            })
        });
        return await res.json();
    },

    async switchSession(sessionId) {
        this.currentSessionId = sessionId;
        const res = await fetch(`/api/v1/sessions/${sessionId}`);
        const sessionDetail = await res.json();

        // Reposition camera
        if (sessionDetail.centerLat && sessionDetail.centerLng) {
            this.map.setView([sessionDetail.centerLat, sessionDetail.centerLng], sessionDetail.zoomLevel || 12);
        }

        // Render layers in sidebar
        this.renderLayers(sessionDetail.layers || []);
    },

    renderLayers(layers) {
        const container = document.getElementById('layer-list');
        container.innerHTML = '';

        // Clear existing map layers
        this.layersMap.forEach(grp => this.map.removeLayer(grp));
        this.layersMap.clear();

        if (layers.length === 0) {
            container.innerHTML = '<div style="color:var(--text-muted); font-size:0.85rem;">No layers yet. Click + Layer to add one.</div>';
            return;
        }

        layers.forEach((layer, idx) => {
            const card = document.createElement('div');
            card.className = `layer-card ${idx === 0 ? 'selected' : ''}`;
            card.innerHTML = `
                <div>
                    <strong>${layer.name}</strong>
                    <div style="font-size:0.75rem; color:var(--text-muted);">${layer.layerType}</div>
                </div>
                <input type="checkbox" ${layer.visible ? 'checked' : ''} data-layer-id="${layer.id}" />
            `;

            if (idx === 0) {
                this.currentLayerId = layer.id;
            }

            card.addEventListener('click', (e) => {
                if (e.target.tagName !== 'INPUT') {
                    document.querySelectorAll('.layer-card').forEach(c => c.classList.remove('selected'));
                    card.classList.add('selected');
                    this.currentLayerId = layer.id;
                }
            });

            card.querySelector('input').addEventListener('change', (e) => {
                const grp = this.layersMap.get(layer.id);
                if (grp) {
                    if (e.target.checked) {
                        this.map.addLayer(grp);
                    } else {
                        this.map.removeLayer(grp);
                    }
                }
            });

            container.appendChild(card);
            this.fetchAndRenderLayerGeoJson(layer.id);
        });
    },

    async fetchAndRenderLayerGeoJson(layerId) {
        try {
            const res = await fetch(`/api/v1/layers/${layerId}/entities`);
            const geoJson = await res.json();

            const geoJsonLayer = L.geoJSON(geoJson, {
                style: (feature) => ({
                    color: feature.properties.strokeColor || '#2A9D8F',
                    fillColor: feature.properties.fillColor || '#2A9D8F33',
                    weight: feature.properties.strokeWidth || 2
                }),
                onEachFeature: (feature, layer) => {
                    if (feature.properties && feature.properties.name) {
                        layer.bindPopup(`<strong>${feature.properties.name}</strong><br>Type: ${feature.properties.entityType}`);
                    }
                }
            });

            geoJsonLayer.addTo(this.map);
            this.layersMap.set(layerId, geoJsonLayer);
        } catch (err) {
            console.error("Failed to fetch layer GeoJSON:", err);
        }
    },

    async promptNewSession() {
        const title = prompt("Enter Map Session Title:", "New Operations Grid");
        if (title) {
            const created = await this.createSession(title, 48.1800, 8.2000, 12);
            await this.loadSessions();
            this.switchSession(created.id);
        }
    },

    async promptNewLayer() {
        if (!this.currentSessionId) return;
        const name = prompt("Enter Layer Name:", "Operational Zone");
        if (name) {
            await fetch(`/api/v1/sessions/${this.currentSessionId}/layers`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name: name,
                    layerType: "STANDARD",
                    orderIndex: this.layersMap.size + 1
                })
            });
            this.switchSession(this.currentSessionId);
        }
    }
};

window.addEventListener('DOMContentLoaded', () => {
    CanvaisApp.init();
});
