// CANVAiS MCP & AI Skill Plugin Settings Manager
const McpSettings = {
    servers: [],

    async init() {
        const btnSettings = document.getElementById('btn-mcp-settings');
        btnSettings.addEventListener('click', () => {
            this.promptAddServer();
        });

        await this.loadServers();
    },

    async loadServers() {
        try {
            const res = await fetch('/api/v1/integrations/mcp');
            this.servers = await res.json();
            this.renderStatus();
        } catch (err) {
            console.error("Failed to load MCP servers:", err);
        }
    },

    renderStatus() {
        const container = document.getElementById('mcp-status-container');
        container.innerHTML = '';

        if (this.servers.length === 0) {
            container.innerHTML = '<div style="font-size:0.8rem; color:var(--text-muted);">No external MCP servers. Click Settings to connect one.</div>';
            return;
        }

        this.servers.forEach(s => {
            const card = document.createElement('div');
            card.className = 'mcp-card';
            card.style.display = 'flex';
            card.style.alignItems = 'center';
            card.style.gap = '8px';
            card.style.padding = '6px';
            card.style.fontSize = '0.8rem';
            card.style.background = 'var(--bg-dark)';
            card.style.borderRadius = '4px';
            card.style.marginBottom = '6px';

            const dotColor = s.status === 'CONNECTED' ? '#2A9D8F' : '#E63946';
            card.innerHTML = `
                <span style="display:inline-block; width:8px; height:8px; border-radius:50%; background:${dotColor};"></span>
                <span style="flex:1;">${s.name}</span>
                <button class="btn btn-sm btn-outline" style="font-size:0.7rem; padding:2px 6px;">Discover</button>
            `;

            card.querySelector('button').addEventListener('click', async () => {
                const res = await fetch(`/api/v1/integrations/mcp/${s.id}/discover`, { method: 'POST' });
                const disc = await res.json();
                alert(`Discovered ${disc.tools.length} tools for ${s.name}`);
            });

            container.appendChild(card);
        });
    },

    async promptAddServer() {
        const name = prompt("Enter MCP Server Name:", "Emergency Weather MCP");
        if (!name) return;
        const uri = prompt("Enter MCP Server Endpoint URI (SSE or Stdio):", "http://localhost:8081/mcp/sse");
        if (!uri) return;

        try {
            await fetch('/api/v1/integrations/mcp', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({
                    name: name,
                    transportType: "SSE",
                    endpointUri: uri
                })
            });
            await this.loadServers();
        } catch (err) {
            console.error("Failed to add MCP server:", err);
        }
    }
};

window.addEventListener('DOMContentLoaded', () => {
    McpSettings.init();
});
