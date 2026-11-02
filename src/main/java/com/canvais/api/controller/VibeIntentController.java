package com.canvais.api.controller;

import com.canvais.api.dto.request.AcceptProposalRequest;
import com.canvais.api.dto.request.VibeIntentProposeRequest;
import com.canvais.api.dto.response.VibeIntentProposalResponse;
import com.canvais.infrastructure.ai.SpeechTranscriptionAdapter;
import com.canvais.service.VibeIntentFusionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for multimodal vibe transcription, proposal synthesis, and acceptance.
 */
@RestController
@RequestMapping("/api/v1/vibe")
@Tag(name = "Vibe & Intent Fusion", description = "Endpoints for speech vibe, freehand gesture fusion, and proposals")
public class VibeIntentController {

    private final VibeIntentFusionService fusionService;
    private final SpeechTranscriptionAdapter speechTranscriptionAdapter;

    public VibeIntentController(VibeIntentFusionService fusionService,
                                SpeechTranscriptionAdapter speechTranscriptionAdapter) {
        this.fusionService = fusionService;
        this.speechTranscriptionAdapter = speechTranscriptionAdapter;
    }

    /**
     * Synthesizes candidate map entities from multimodal vibe and gesture input.
     *
     * @param request multimodal payload
     * @return 200 OK with proposed entities
     */
    @PostMapping("/propose")
    @Operation(summary = "Submit vibe text and gesture strokes to generate a spatial proposal")
    public ResponseEntity<VibeIntentProposalResponse> proposeVibeIntent(
            @Valid @RequestBody VibeIntentProposeRequest request) {
        return ResponseEntity.ok(fusionService.generateProposal(request));
    }

    /**
     * Transcribes an uploaded audio recording stream.
     *
     * @param audio raw audio file
     * @return 200 OK with transcript
     */
    @PostMapping(value = "/transcribe", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload audio stream to transcribe naturally spoken vibe")
    public ResponseEntity<Map<String, Object>> transcribeAudio(@RequestParam("audio") MultipartFile audio) throws IOException {
        return ResponseEntity.ok(speechTranscriptionAdapter.transcribe(audio.getBytes()));
    }

    /**
     * Accepts and commits proposed entities into the designated layer.
     *
     * @param proposalId proposal UUID
     * @param request optional selection parameters
     * @return 200 OK with committed entity IDs
     */
    @PostMapping("/proposals/{proposalId}/accept")
    @Operation(summary = "Accept and commit proposed entities into the layer")
    public ResponseEntity<Map<String, Object>> acceptProposal(
            @PathVariable UUID proposalId,
            @RequestBody(required = false) AcceptProposalRequest request) {
        List<UUID> committedIds = fusionService.acceptProposal(proposalId, request);
        return ResponseEntity.ok(Map.of(
                "committedCount", committedIds.size(),
                "entityIds", committedIds
        ));
    }
}
