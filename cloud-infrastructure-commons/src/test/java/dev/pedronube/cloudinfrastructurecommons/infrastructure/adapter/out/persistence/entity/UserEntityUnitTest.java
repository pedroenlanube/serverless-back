package dev.pedronube.cloudinfrastructurecommons.infrastructure.adapter.out.persistence.entity;

import dev.pedronube.cloudinfrastructurecommons.infrastructure.adapter.out.persistence.entity.mapper.UserEntityFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserEntity Unit Tests")
class UserEntityUnitTest {

    @Nested
    @DisplayName("Factory method fromCognitoUser")
    class FromCognitoUserTests {

        @Test
        @DisplayName("Should create UserEntity with all required fields")
        void shouldCreateUserEntityWithAllRequiredFields() {
            // Given
            String sub = "cognito-sub-123";
            String username = "testuser";
            String email = "test@example.com";

            // When
            UserEntity result = UserEntityFactory.fromCognitoData(sub, username, email);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSub()).isEqualTo(sub);
            assertThat(result.getUsername()).isEqualTo(username);
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getSubscriptionLevel()).isEqualTo("FREE");
            assertThat(result.getCreatedAt()).isNotNull();
            assertThat(result.getPk()).isEqualTo("USER#" + sub);
            assertThat(result.getSk()).isEqualTo("PROFILE");
        }

        @Test
        @DisplayName("Should set default subscription level to FREE")
        void shouldSetDefaultSubscriptionLevelToFree() {
            // Given
            String sub = "sub-456";
            String username = "user456";
            String email = "user456@example.com";

            // When
            UserEntity result = UserEntityFactory.fromCognitoData(sub, username, email);

            // Then
            assertThat(result.getSubscriptionLevel()).isEqualTo("FREE");
        }

        @Test
        @DisplayName("Should set createdAt to current timestamp")
        void shouldSetCreatedAtToCurrentTimestamp() {
            // Given
            String sub = "sub-789";
            String username = "user789";
            String email = "user789@example.com";
            Instant beforeCreation = Instant.now();

            // When
            UserEntity result = UserEntityFactory.fromCognitoData(sub, username, email);

            // Then
            Instant afterCreation = Instant.now();
            Instant createdAt = Instant.parse(result.getCreatedAt());
            
            assertThat(createdAt)
                    .isAfterOrEqualTo(beforeCreation)
                    .isBeforeOrEqualTo(afterCreation);
        }

        @Test
        @DisplayName("Should set correct DynamoDB keys")
        void shouldSetCorrectDynamoDbKeys() {
            // Given
            String sub = "unique-sub-id";
            String username = "uniqueuser";
            String email = "unique@example.com";

            // When
            UserEntity result = UserEntityFactory.fromCognitoData(sub, username, email);

            // Then
            assertThat(result.getPk()).isEqualTo("USER#unique-sub-id");
            assertThat(result.getSk()).isEqualTo("PROFILE");
        }

        @Test
        @DisplayName("Should handle null sub parameter")
        void shouldHandleNullSubParameter() {
            // Given
            String sub = null;
            String username = "testuser";
            String email = "test@example.com";

            // When
            UserEntity result = UserEntityFactory.fromCognitoData(sub, username, email);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSub()).isNull();
            assertThat(result.getUsername()).isEqualTo(username);
            assertThat(result.getEmail()).isEqualTo(email);
            assertThat(result.getPk()).isEqualTo("USER#null");
        }

        @Test
        @DisplayName("Should handle null username parameter")
        void shouldHandleNullUsernameParameter() {
            // Given
            String sub = "sub-123";
            String username = null;
            String email = "test@example.com";

            // When
            UserEntity result = UserEntityFactory.fromCognitoData(sub, username, email);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSub()).isEqualTo(sub);
            assertThat(result.getUsername()).isNull();
            assertThat(result.getEmail()).isEqualTo(email);
        }

        @Test
        @DisplayName("Should handle null email parameter")
        void shouldHandleNullEmailParameter() {
            // Given
            String sub = "sub-123";
            String username = "testuser";
            String email = null;

            // When
            UserEntity result = UserEntityFactory.fromCognitoData(sub, username, email);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSub()).isEqualTo(sub);
            assertThat(result.getUsername()).isEqualTo(username);
            assertThat(result.getEmail()).isNull();
        }

        @Test
        @DisplayName("Should handle empty string parameters")
        void shouldHandleEmptyStringParameters() {
            // Given
            String sub = "";
            String username = "";
            String email = "";

            // When
            UserEntity result = UserEntityFactory.fromCognitoData(sub, username, email);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSub()).isEmpty();
            assertThat(result.getUsername()).isEmpty();
            assertThat(result.getEmail()).isEmpty();
            assertThat(result.getPk()).isEqualTo("USER#");
        }
    }

    @Nested
    @DisplayName("Entity properties")
    class EntityPropertiesTests {

        @Test
        @DisplayName("Should allow modification of subscription level")
        void shouldAllowModificationOfSubscriptionLevel() {
            // Given
            UserEntity entity = UserEntityFactory.fromCognitoData("sub-123", "user", "user@example.com");

            // When
            entity.setSubscriptionLevel("PREMIUM");

            // Then
            assertThat(entity.getSubscriptionLevel()).isEqualTo("PREMIUM");
        }

        @Test
        @DisplayName("Should allow modification of createdAt")
        void shouldAllowModificationOfCreatedAt() {
            // Given
            UserEntity entity = UserEntityFactory.fromCognitoData("sub-123", "user", "user@example.com");
            String newCreatedAt = "2024-01-01T00:00:00Z";

            // When
            entity.setCreatedAt(newCreatedAt);

            // Then
            assertThat(entity.getCreatedAt()).isEqualTo(newCreatedAt);
        }

        @Test
        @DisplayName("Should inherit from BaseEntity")
        void shouldInheritFromBaseEntity() {
            // Given
            UserEntity entity = UserEntityFactory.fromCognitoData("sub-123", "user", "user@example.com");

            // When & Then
            assertThat(entity).isInstanceOf(BaseEntity.class);
            assertThat(entity.getPk()).isNotNull();
            assertThat(entity.getSk()).isNotNull();
        }
    }
}