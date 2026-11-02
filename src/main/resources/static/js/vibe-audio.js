// CANVAiS Vibe Audio & Text Capture
const VibeAudioClient = {
    isRecording: false,
    mediaRecorder: null,
    audioChunks: [],

    init() {
        const btnRecord = document.getElementById('btn-vibe-record');
        const vibeBar = document.getElementById('vibe-floating-bar');
        const btnSubmit = document.getElementById('btn-submit-vibe');
        const btnCancel = document.getElementById('btn-cancel-vibe');

        btnRecord.addEventListener('click', () => {
            this.toggleVibeBar(true);
            this.startVoiceCapture();
        });

        btnCancel.addEventListener('click', () => {
            this.toggleVibeBar(false);
            this.stopVoiceCapture();
            GestureRecorder.clearCanvas();
        });

        btnSubmit.addEventListener('click', () => {
            this.submitIntent();
        });
    },

    toggleVibeBar(show) {
        const bar = document.getElementById('vibe-floating-bar');
        if (show) {
            bar.classList.remove('hidden');
            // Auto switch to draw mode
            document.getElementById('btn-mode-draw').click();
        } else {
            bar.classList.add('hidden');
        }
    },

    async startVoiceCapture() {
        if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
            console.warn("MediaDevices API unavailable, falling back to text vibe.");
            return;
        }
        try {
            const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
            this.audioChunks = [];
            this.mediaRecorder = new MediaRecorder(stream);
            this.mediaRecorder.ondataavailable = (e) => this.audioChunks.push(e.data);
            this.mediaRecorder.start();
            this.isRecording = true;
        } catch (err) {
            console.warn("Microphone access denied or unavailable:", err);
        }
    },

    stopVoiceCapture() {
        if (this.mediaRecorder && this.isRecording) {
            this.mediaRecorder.stop();
            this.isRecording = false;
        }
    },

    async submitIntent() {
        const textInput = document.getElementById('vibe-text-input');
        let vibeText = textInput.value.trim();

        // If audio recorded and text empty, transcribe audio
        if (!vibeText && this.audioChunks.length > 0) {
            const audioBlob = new Blob(this.audioChunks, { type: 'audio/webm' });
            const formData = new FormData();
            formData.append('audio', audioBlob, 'vibe.webm');

            try {
                const res = await fetch('/api/v1/vibe/transcribe', {
                    method: 'POST',
                    body: formData
                });
                const data = await res.json();
                vibeText = data.transcript;
                textInput.value = vibeText;
            } catch (err) {
                console.error("Transcription failed:", err);
            }
        }

        if (!vibeText) {
            vibeText = "Draw an operational area based on gesture";
        }

        // Send propose request
        const bounds = CanvaisApp.map.getBounds();
        const payload = {
            sessionId: CanvaisApp.currentSessionId,
            targetLayerId: CanvaisApp.currentLayerId,
            textVibe: vibeText,
            gestureStrokes: GestureRecorder.getStrokes(),
            viewportBounds: {
                north: bounds.getNorth(),
                south: bounds.getSouth(),
                east: bounds.getEast(),
                west: bounds.getWest()
            }
        };

        try {
            const res = await fetch('/api/v1/vibe/propose', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(payload)
            });
            const proposal = await res.json();
            this.toggleVibeBar(false);
            ProposalReviewer.showProposal(proposal);
        } catch (err) {
            console.error("Proposal generation failed:", err);
        }
    }
};

window.addEventListener('DOMContentLoaded', () => {
    VibeAudioClient.init();
});
