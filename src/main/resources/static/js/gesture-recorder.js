// CANVAiS Freehand Gesture Recorder
const GestureRecorder = {
    canvas: null,
    ctx: null,
    isRecording: false,
    strokes: [], // list of {lat, lng, timestampMs}

    init() {
        this.canvas = document.getElementById('gesture-canvas');
        this.ctx = this.canvas.getContext('2d');
        this.resizeCanvas();
        window.addEventListener('resize', () => this.resizeCanvas());

        this.canvas.addEventListener('mousedown', (e) => this.startStroke(e));
        this.canvas.addEventListener('mousemove', (e) => this.addPoint(e));
        this.canvas.addEventListener('mouseup', () => this.endStroke());
    },

    resizeCanvas() {
        const wrapper = document.getElementById('map-viewport-wrapper');
        this.canvas.width = wrapper.clientWidth;
        this.canvas.height = wrapper.clientHeight;
    },

    startStroke(e) {
        if (!CanvaisApp.isDrawingMode) return;
        this.isRecording = true;
        this.strokes = [];
        this.clearCanvas();
        this.addPoint(e);
    },

    addPoint(e) {
        if (!this.isRecording) return;
        const rect = this.canvas.getBoundingClientRect();
        const x = e.clientX - rect.left;
        const y = e.clientY - rect.top;

        // Convert screen pixel to map coordinate
        const latLng = CanvaisApp.map.containerPointToLatLng(L.point(x, y));
        this.strokes.push({
            lat: latLng.lat,
            lng: latLng.lng,
            timestampMs: Date.now()
        });

        // Draw visual feedback trail
        this.ctx.lineWidth = 3;
        this.ctx.lineCap = 'round';
        this.ctx.strokeStyle = '#E76F51';

        if (this.strokes.length === 1) {
            this.ctx.beginPath();
            this.ctx.moveTo(x, y);
        } else {
            this.ctx.lineTo(x, y);
            this.ctx.stroke();
        }
    },

    endStroke() {
        this.isRecording = false;
        console.log(`Captured gesture with ${this.strokes.length} points.`);
    },

    clearCanvas() {
        if (this.ctx && this.canvas) {
            this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        }
    },

    getStrokes() {
        return this.strokes;
    }
};

window.addEventListener('DOMContentLoaded', () => {
    GestureRecorder.init();
});
