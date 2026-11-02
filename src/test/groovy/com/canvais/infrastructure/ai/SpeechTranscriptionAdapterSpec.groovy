package com.canvais.infrastructure.ai

import com.canvais.SpecificationBase

/**
 * Spock specification testing speech transcription adapter.
 */
class SpeechTranscriptionAdapterSpec extends SpecificationBase {

    SpeechTranscriptionAdapter adapter = new SpeechTranscriptionAdapter()

    def "should transcribe audio bytes and return transcript with confidence"() {
        given: "dummy audio stream"
        byte[] audioBytes = "MOCK_AUDIO_DATA".getBytes()

        when: "transcribe is called"
        def result = adapter.transcribe(audioBytes)

        then: "result contains non-empty transcript and confidence above 0.8"
        result.containsKey("transcript")
        result.containsKey("confidence")
        result.confidence >= 0.80
    }

    def "should throw IllegalArgumentException on empty audio"() {
        when: "empty bytes passed"
        adapter.transcribe(new byte[0])

        then: "exception thrown"
        thrown(IllegalArgumentException)
    }
}
