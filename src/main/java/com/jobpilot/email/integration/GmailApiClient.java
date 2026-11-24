
package com.jobpilot.email.integration;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonProperty;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Component
@RequiredArgsConstructor
public class GmailApiClient {

    private final RestClient.Builder restClientBuilder;

    public GmailSendResponse sendMessage(String accessToken, String rawMessage) {

        GmailSendRequest request = new GmailSendRequest();
        request.setRaw(rawMessage);

        return restClientBuilder.build()
                .post()
                .uri("https://gmail.googleapis.com/gmail/v1/users/me/messages/send")
                .headers(headers ->
                        headers.setBearerAuth(accessToken))
                .contentType(MediaType.APPLICATION_JSON)
                .body(request)
                .retrieve()
                .body(GmailSendResponse.class);
    }

    @Getter
    @Setter
    public static class GmailSendRequest {

        private String raw;
    }

    @Getter
    @Setter
    public static class GmailSendResponse {

        private String id;

        private String threadId;

        @JsonProperty("labelIds")
        private java.util.List<String> labelIds;
    }
}
