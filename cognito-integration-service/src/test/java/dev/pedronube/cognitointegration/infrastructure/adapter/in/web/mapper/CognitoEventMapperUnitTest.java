package dev.pedronube.cognitointegration.infrastructure.adapter.in.web.mapper;

import com.amazonaws.services.lambda.runtime.events.CognitoUserPoolPostConfirmationEvent;
import dev.pedronube.domaincommons.domain.model.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("CognitoEventMapper Unit Tests")
class CognitoEventMapperUnitTest {

    @Nested
    @DisplayName("toDomain conversion")
    class ToDomainTests {

        @Test
        @DisplayName("Should convert CognitoUserPoolPostConfirmationEvent to User successfully")
        void shouldConvertCognitoEventToUser() {
            // Given
            CognitoUserPoolPostConfirmationEvent event = createCognitoEvent(
                "cognito-sub-123",
                "testuser",
                "test@example.com"
            );

            Instant beforeConversion = Instant.now();

            // When
            User result = CognitoEventMapper.toDomain.apply(event);

            // Then
            Instant afterConversion = Instant.now();
            
            assertThat(result).isNotNull();
            assertThat(result.getSub()).isEqualTo("cognito-sub-123");
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            assertThat(result.getSubscriptionLevel()).isEqualTo("FREE");
            assertThat(result.getCreatedAt()).isNotNull();
            
            Instant createdAt = Instant.parse(result.getCreatedAt());
            assertThat(createdAt)
                    .isAfterOrEqualTo(beforeConversion)
                    .isBeforeOrEqualTo(afterConversion);
        }

        @Test
        @DisplayName("Should return null when event is null")
        void shouldReturnNullWhenEventIsNull() {
            // Given
            CognitoUserPoolPostConfirmationEvent event = null;

            // When
            User result = CognitoEventMapper.toDomain.apply(event);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle event with null request")
        void shouldHandleEventWithNullRequest() {
            // Given
            CognitoUserPoolPostConfirmationEvent event = new CognitoUserPoolPostConfirmationEvent();
            event.setUserName("testuser");
            event.setRequest(null);

            // When
            User result = CognitoEventMapper.toDomain.apply(event);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle event with null user attributes")
        void shouldHandleEventWithNullUserAttributes() {
            // Given
            CognitoUserPoolPostConfirmationEvent event = new CognitoUserPoolPostConfirmationEvent();
            event.setUserName("testuser");
            
            CognitoUserPoolPostConfirmationEvent.Request request = 
                new CognitoUserPoolPostConfirmationEvent.Request();
            request.setUserAttributes(null);
            event.setRequest(request);

            // When
            User result = CognitoEventMapper.toDomain.apply(event);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle event with empty user attributes")
        void shouldHandleEventWithEmptyUserAttributes() {
            // Given
            CognitoUserPoolPostConfirmationEvent event = new CognitoUserPoolPostConfirmationEvent();
            event.setUserName("testuser");
            
            CognitoUserPoolPostConfirmationEvent.Request request = 
                new CognitoUserPoolPostConfirmationEvent.Request();
            request.setUserAttributes(new HashMap<>());
            event.setRequest(request);

            // When
            User result = CognitoEventMapper.toDomain.apply(event);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSub()).isNull();
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getEmail()).isNull();
            assertThat(result.getSubscriptionLevel()).isEqualTo("FREE");
        }

        @Test
        @DisplayName("Should handle event with null username")
        void shouldHandleEventWithNullUsername() {
            // Given
            CognitoUserPoolPostConfirmationEvent event = createCognitoEvent(
                "sub-123",
                null,
                "test@example.com"
            );

            // When
            User result = CognitoEventMapper.toDomain.apply(event);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSub()).isEqualTo("sub-123");
            assertThat(result.getUsername()).isNull();
            assertThat(result.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should handle event with missing sub attribute")
        void shouldHandleEventWithMissingSubAttribute() {
            // Given
            CognitoUserPoolPostConfirmationEvent event = new CognitoUserPoolPostConfirmationEvent();
            event.setUserName("testuser");
            
            CognitoUserPoolPostConfirmationEvent.Request request = 
                new CognitoUserPoolPostConfirmationEvent.Request();
            Map<String, String> attributes = new HashMap<>();
            attributes.put("email", "test@example.com");
            // Note: "sub" attribute is missing
            request.setUserAttributes(attributes);
            event.setRequest(request);

            // When
            User result = CognitoEventMapper.toDomain.apply(event);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSub()).isNull();
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Should handle event with missing email attribute")
        void shouldHandleEventWithMissingEmailAttribute() {
            // Given
            CognitoUserPoolPostConfirmationEvent event = new CognitoUserPoolPostConfirmationEvent();
            event.setUserName("testuser");
            
            CognitoUserPoolPostConfirmationEvent.Request request = 
                new CognitoUserPoolPostConfirmationEvent.Request();
            Map<String, String> attributes = new HashMap<>();
            attributes.put("sub", "sub-123");
            // Note: "email" attribute is missing
            request.setUserAttributes(attributes);
            event.setRequest(request);

            // When
            User result = CognitoEventMapper.toDomain.apply(event);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSub()).isEqualTo("sub-123");
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getEmail()).isNull();
        }

        @Test
        @DisplayName("Should always set subscription level to FREE")
        void shouldAlwaysSetSubscriptionLevelToFree() {
            // Given
            CognitoUserPoolPostConfirmationEvent event = createCognitoEvent(
                "sub-123",
                "testuser",
                "test@example.com"
            );

            // When
            User result = CognitoEventMapper.toDomain.apply(event);

            // Then
            assertThat(result.getSubscriptionLevel()).isEqualTo("FREE");
        }

        @Test
        @DisplayName("Should set createdAt to current timestamp")
        void shouldSetCreatedAtToCurrentTimestamp() {
            // Given
            CognitoUserPoolPostConfirmationEvent event = createCognitoEvent(
                "sub-123",
                "testuser",
                "test@example.com"
            );
            Instant beforeConversion = Instant.now();

            // When
            User result = CognitoEventMapper.toDomain.apply(event);

            // Then
            Instant afterConversion = Instant.now();
            Instant createdAt = Instant.parse(result.getCreatedAt());
            
            assertThat(createdAt)
                    .isAfterOrEqualTo(beforeConversion)
                    .isBeforeOrEqualTo(afterConversion);
        }
    }

    @Nested
    @DisplayName("Edge cases and error handling")
    class EdgeCasesTests {

        @Test
        @DisplayName("Should handle event with empty string attributes")
        void shouldHandleEventWithEmptyStringAttributes() {
            // Given
            CognitoUserPoolPostConfirmationEvent event = createCognitoEvent("", "", "");

            // When
            User result = CognitoEventMapper.toDomain.apply(event);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSub()).isEmpty();
            assertThat(result.getUsername()).isEmpty();
            assertThat(result.getEmail()).isEmpty();
            assertThat(result.getSubscriptionLevel()).isEqualTo("FREE");
        }

        @Test
        @DisplayName("Should handle event with whitespace-only attributes")
        void shouldHandleEventWithWhitespaceOnlyAttributes() {
            // Given
            CognitoUserPoolPostConfirmationEvent event = createCognitoEvent("   ", "   ", "   ");

            // When
            User result = CognitoEventMapper.toDomain.apply(event);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSub()).isEqualTo("   ");
            assertThat(result.getUsername()).isEqualTo("   ");
            assertThat(result.getEmail()).isEqualTo("   ");
        }
    }

    // Helper method to create CognitoUserPoolPostConfirmationEvent
    private CognitoUserPoolPostConfirmationEvent createCognitoEvent(String sub, String username, String email) {
        CognitoUserPoolPostConfirmationEvent event = new CognitoUserPoolPostConfirmationEvent();
        event.setUserName(username);
        
        CognitoUserPoolPostConfirmationEvent.Request request = 
            new CognitoUserPoolPostConfirmationEvent.Request();
        
        Map<String, String> attributes = new HashMap<>();
        if (sub != null) {
            attributes.put("sub", sub);
        }
        if (email != null) {
            attributes.put("email", email);
        }
        
        request.setUserAttributes(attributes);
        event.setRequest(request);
        
        return event;
    }
}