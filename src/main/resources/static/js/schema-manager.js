// CANVAiS Semantic Schema Manager
const SchemaManager = {
    schemas: [],

    async init() {
        await this.loadSchemas();
    },

    async loadSchemas() {
        try {
            const res = await fetch('/api/v1/integrations/schemas');
            this.schemas = await res.json();
            console.log(`Loaded ${this.schemas.length} semantic schemas.`);
        } catch (err) {
            console.error("Failed to load schemas:", err);
        }
    },

    async createCustomSchema(name, domainCategory, entityTypeName, geometryType) {
        const payload = {
            name: name,
            domainCategory: domainCategory,
            version: "1.0.0",
            entityTypes: [{
                name: entityTypeName,
                displayName: entityTypeName,
                allowedGeometries: [geometryType],
                attributes: [
                    { name: "priority", dataType: "STRING", required: true, allowedValues: ["LOW", "HIGH"] }
                ]
            }]
        };

        const res = await fetch('/api/v1/integrations/schemas', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        const created = await res.json();
        this.schemas.push(created);
        return created;
    }
};

window.addEventListener('DOMContentLoaded', () => {
    SchemaManager.init();
});
