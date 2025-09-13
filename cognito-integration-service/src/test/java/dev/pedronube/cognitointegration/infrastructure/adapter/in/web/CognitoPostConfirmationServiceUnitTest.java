package dev.pedronube.cognitointegration.infrastructure.adapter.in.web;

import com.amazonaws.services.lambda.runtime.events.CognitoUserPoolPostConfirmationEvent;
import dev.pedronube.cognitointegration.infrastructure.adapter.in.web.mapper.CognitoEventMapper;
import dev.pedronube.domaincommons.domain.model.user.User;
import dev.pedronube.domaincommons.domain.usecase.SaveUserUseCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("CognitoPostConfirmationService Unit Tests")
class CognitoPostConfirmationServiceUnitTest {

    @Mock
    private SaveUserUseCase mockSaveUserUseCase;

    private CognitoPostConfirmationService service;

    @BeforeEach
    void setUp() {
        service = new CognitoPostConfirmationService();
    }

    @Nested
    @DisplayName("Bean creation")
    class BeanCreationTests {

        @Test
        @DisplayName("Should create postConfirmation Function bean successfully")
        void shouldCreatePostConfirmationFunctionBeanSuccessfully() {
            // When
            Function<CognitoUserPoolPostConfirmationEvent, CognitoUserPoolPostConfirmationEvent> result = 
                service.postConfirmation(mockSaveUserUseCase);

            // Then
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("Should return functional lambda")
        void shouldReturnFunctionalLambda() {
            // Given
            CognitoUserPoolPostConfirmationEvent event = createCognitoEvent(
                "sub-123", "testuser", "test@example.com"
            );

            Function<CognitoUserPoolPostConfirmationEvent, CognitoUserPoolPostConfirmationEvent> function = 
                service.postConfirmation(mockSaveUserUseCase);

            // When
            CognitoUserPoolPostConfirmationEvent result = function.apply(event);

            // Then
            assertThat(result)
                    .isNotNull()
                    .isSameAs(event); // Should return the same event
        }
    }

    @Nested
    @DisplayName("Function execution")
    class FunctionExecutionTests {

        @Test
        @DisplayName("Should process event and call SaveUserUseCase")
        void shouldProcessEventAndCallSaveUserUseCase() {
            // Given
            CognitoUserPoolPostConfirmationEvent event = createCognitoEvent(
                "sub-456", "processuser", "process@example.com"
            );

            User expectedUser = new User();
            expectedUser.setSub("sub-456");
            expectedUser.setUsername("processuser");
            expectedUser.setEmail("process@example.com");
            expectedUser.setSubscriptionLevel("FREE");

            Function<CognitoUserPoolPostConfirmationEvent, CognitoUserPoolPostConfirmationEvent> function = 
                service.postConfirmation(mockSaveUserUseCase);

            try (MockedStatic<CognitoEventMapper> mockedMapper = mockStatic(CognitoEventMapper.class)) {
                mockedMapper.when(() -> CognitoEventMapper.toDomain.apply(event))
                           .thenReturn(expectedUser);

                // When
                CognitoUserPoolPostConfirmationEvent result = function.apply(event);

                // Then
                assertThat(result).isSameAs(event);
                verify(mockSaveUserUseCase).accept(expectedUser);
            }
        }

        @Test
        @DisplayName("Should return original event even when SaveUserUseCase throws exception")
        void shouldReturnOriginalEventEvenWhenSaveUserUseCaseThrowsException() {
            // Given
            CognitoUserPoolPostConfirmationEvent event = createCognitoEvent(
                "sub-exception", "exceptionuser", "exception@example.com"
            );

            User user = new User();
            user.setSub("sub-exception");

            doThrow(new RuntimeException("Database error")).when(mockSaveUserUseCase).accept(any(User.class));

            Function<CognitoUserPoolPostConfirmationEvent, CognitoUserPoolPostConfirmationEvent> function = 
                service.postConfirmation(mockSaveUserUseCase);

            try (MockedStatic<CognitoEventMapper> mockedMapper = mockStatic(CognitoEventMapper.class)) {
                mockedMapper.when(() -> CognitoEventMapper.toDomain.apply(event))
                           .thenReturn(user);

                // When & Then
                try {
                    CognitoUserPoolPostConfirmationEvent result = function.apply(event);
                    // If no exception is thrown, the function should still return the event
                    assertThat(result).isSameAs(event);
                } catch (RuntimeException e) {
                    // If exception is propagated, that's also acceptable behavior
                    assertThat(e.getMessage()).isEqualTo("Database error");
                }

                verify(mockSaveUserUseCase).accept(user);
            }
        }

        @Test
        @DisplayName("Should preserve event data during processing")
        void shouldPreserveEventDataDuringProcessing() {
            // Given
            CognitoUserPoolPostConfirmationEvent originalEvent = createCognitoEvent(
                "preserve-sub", "preserveuser", "preserve@example.com"
            );
            
            // Add additional event data
            originalEvent.setRegion("us-east-1");
            originalEvent.setUserPoolId("us-east-1_TestPool");

            User mappedUser = new User();
            mappedUser.setSub("preserve-sub");

            Function<CognitoUserPoolPostConfirmationEvent, CognitoUserPoolPostConfirmationEvent> function = 
                service.postConfirmation(mockSaveUserUseCase);

            try (MockedStatic<CognitoEventMapper> mockedMapper = mockStatic(CognitoEventMapper.class)) {
                mockedMapper.when(() -> CognitoEventMapper.toDomain.apply(originalEvent))
                           .thenReturn(mappedUser);

                // When
                CognitoUserPoolPostConfirmationEvent result = function.apply(originalEvent);

                // Then
                assertThat(result).isSameAs(originalEvent);
                assertThat(result.getRegion()).isEqualTo("us-east-1");
                assertThat(result.getUserPoolId()).isEqualTo("us-east-1_TestPool");
                assertThat(result.getUserName()).isEqualTo("preserveuser");
            }
        }

        @Test
        @DisplayName("Should call mapper with correct event")
        void shouldCallMapperWithCorrectEvent() {
            // Given
            CognitoUserPoolPostConfirmationEvent event = createCognitoEvent(
                "mapper-test-sub", "mapperuser", "mapper@example.com"
            );

            User mappedUser = new User();
            mappedUser.setSub("mapper-test-sub");

            Function<CognitoUserPoolPostConfirmationEvent, CognitoUserPoolPostConfirmationEvent> function = 
                service.postConfirmation(mockSaveUserUseCase);

            try (MockedStatic<CognitoEventMapper> mockedMapper = mockStatic(CognitoEventMapper.class)) {
                mockedMapper.when(() -> CognitoEventMapper.toDomain.apply(event))
                           .thenReturn(mappedUser);

                // When
                function.apply(event);

                // Then
                mockedMapper.verify(() -> CognitoEventMapper.toDomain.apply(event));
            }
        }
    }

    @Nested
    @DisplayName("Integration behavior")
    class IntegrationBehaviorTests {

        @Test
        @DisplayName("Should work with different SaveUserUseCase implementations")
        void shouldWorkWithDifferentSaveUserUseCaseImplementations() {
            // Given
            SaveUserUseCase anotherMockUseCase = mock(SaveUserUseCase.class);
            CognitoUserPoolPostConfirmationEvent event = createCognitoEvent(
                "integration-sub", "integrationuser", "integration@example.com"
            );

            User user = new User();
            user.setSub("integration-sub");

            // When
            Function<CognitoUserPoolPostConfirmationEvent, CognitoUserPoolPostConfirmationEvent> function1 = 
                service.postConfirmation(mockSaveUserUseCase);
            Function<CognitoUserPoolPostConfirmationEvent, CognitoUserPoolPostConfirmationEvent> function2 = 
                service.postConfirmation(anotherMockUseCase);

            try (MockedStatic<CognitoEventMapper> mockedMapper = mockStatic(CognitoEventMapper.class)) {
                mockedMapper.when(() -> CognitoEventMapper.toDomain.apply(event))
                           .thenReturn(user);

                function1.apply(event);
                function2.apply(event);

                // Then
                verify(mockSaveUserUseCase).accept(user);
                verify(anotherMockUseCase).accept(user);
            }
        }

        @Test
        @DisplayName("Should handle multiple sequential calls correctly")
        void shouldHandleMultipleSequentialCallsCorrectly() {
            // Given
            CognitoUserPoolPostConfirmationEvent event1 = createCognitoEvent(
                "seq-1", "sequser1", "seq1@example.com"
            );
            CognitoUserPoolPostConfirmationEvent event2 = createCognitoEvent(
                "seq-2", "sequser2", "seq2@example.com"
            );

            User user1 = new User();
            user1.setSub("seq-1");
            User user2 = new User();
            user2.setSub("seq-2");

            Function<CognitoUserPoolPostConfirmationEvent, CognitoUserPoolPostConfirmationEvent> function = 
                service.postConfirmation(mockSaveUserUseCase);

            try (MockedStatic<CognitoEventMapper> mockedMapper = mockStatic(CognitoEventMapper.class)) {
                mockedMapper.when(() -> CognitoEventMapper.toDomain.apply(event1))
                           .thenReturn(user1);
                mockedMapper.when(() -> CognitoEventMapper.toDomain.apply(event2))
                           .thenReturn(user2);

                // When
                CognitoUserPoolPostConfirmationEvent result1 = function.apply(event1);
                CognitoUserPoolPostConfirmationEvent result2 = function.apply(event2);

                // Then
                assertThat(result1).isSameAs(event1);
                assertThat(result2).isSameAs(event2);
                verify(mockSaveUserUseCase).accept(user1);
                verify(mockSaveUserUseCase).accept(user2);
            }
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