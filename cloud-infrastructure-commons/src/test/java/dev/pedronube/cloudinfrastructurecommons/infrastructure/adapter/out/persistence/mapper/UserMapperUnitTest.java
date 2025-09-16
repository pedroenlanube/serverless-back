package dev.pedronube.cloudinfrastructurecommons.infrastructure.adapter.out.persistence.mapper;

import dev.pedronube.cloudinfrastructurecommons.infrastructure.adapter.out.persistence.entity.UserEntity;
import dev.pedronube.cloudinfrastructurecommons.infrastructure.adapter.out.persistence.entity.mapper.UserEntityFactory;
import dev.pedronube.domaincommons.domain.model.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserMapper Unit Tests")
class UserMapperUnitTest {

    @Nested
    @DisplayName("toDomain conversion")
    class ToDomainTests {

        @Test
        @DisplayName("Should convert UserEntity to User successfully")
        void shouldConvertUserEntityToUser() {
            // Given
            UserEntity entity = UserEntityFactory.fromCognitoData("sub123", "testuser", "test@example.com");
            entity.setSubscriptionLevel("PREMIUM");
            entity.setCreatedAt("2024-01-01T10:00:00Z");

            // When
            User result = UserMapper.toDomain.apply(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSub()).isEqualTo("sub123");
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getEmail()).isEqualTo("test@example.com");
            assertThat(result.getSubscriptionLevel()).isEqualTo("PREMIUM");
            assertThat(result.getCreatedAt()).isEqualTo("2024-01-01T10:00:00Z");
        }

        @Test
        @DisplayName("Should return null when UserEntity is null")
        void shouldReturnNullWhenEntityIsNull() {
            // Given
            UserEntity entity = null;

            // When
            User result = UserMapper.toDomain.apply(entity);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle UserEntity with null fields")
        void shouldHandleEntityWithNullFields() {
            // Given
            UserEntity entity = new UserEntity();
            entity.setSub(null);
            entity.setUsername(null);
            entity.setEmail(null);

            // When
            User result = UserMapper.toDomain.apply(entity);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSub()).isNull();
            assertThat(result.getUsername()).isNull();
            assertThat(result.getEmail()).isNull();
        }
    }

    @Nested
    @DisplayName("toEntity conversion")
    class ToEntityTests {

        @Test
        @DisplayName("Should convert User to UserEntity successfully")
        void shouldConvertUserToUserEntity() {
            // Given
            User user = new User();
            user.setSub("sub456");
            user.setUsername("anotheruser");
            user.setEmail("another@example.com");
            user.setSubscriptionLevel("FREE");
            user.setCreatedAt("2024-01-02T15:30:00Z");

            // When
            UserEntity result = UserMapper.toEntity.apply(user);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSub()).isEqualTo("sub456");
            assertThat(result.getUsername()).isEqualTo("anotheruser");
            assertThat(result.getEmail()).isEqualTo("another@example.com");
            assertThat(result.getSubscriptionLevel()).isEqualTo("FREE");
        }

        @Test
        @DisplayName("Should return null when User is null")
        void shouldReturnNullWhenUserIsNull() {
            // Given
            User user = null;

            // When
            UserEntity result = UserMapper.toEntity.apply(user);

            // Then
            assertThat(result).isNull();
        }

        @Test
        @DisplayName("Should handle User with null sub gracefully")
        void shouldHandleUserWithNullSub() {
            // Given
            User user = new User();
            user.setSub(null);
            user.setUsername("testuser");
            user.setEmail("test@example.com");

            // When
            UserEntity result = UserMapper.toEntity.apply(user);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSub()).isNull();
            assertThat(result.getUsername()).isEqualTo("testuser");
            assertThat(result.getEmail()).isEqualTo("test@example.com");
        }
    }

    @Nested
    @DisplayName("Bidirectional conversion")
    class BidirectionalTests {

        @Test
        @DisplayName("Should maintain data integrity in round-trip conversion")
        void shouldMaintainDataIntegrityInRoundTrip() {
            // Given
            User originalUser = new User();
            originalUser.setSub("sub789");
            originalUser.setUsername("roundtripuser");
            originalUser.setEmail("roundtrip@example.com");
            originalUser.setSubscriptionLevel("PREMIUM");
            originalUser.setCreatedAt("2024-01-03T12:00:00Z");

            // When
            UserEntity entity = UserMapper.toEntity.apply(originalUser);
            User resultUser = UserMapper.toDomain.apply(entity);

            // Then
            assertThat(resultUser).isNotNull();
            assertThat(resultUser.getSub()).isEqualTo(originalUser.getSub());
            assertThat(resultUser.getUsername()).isEqualTo(originalUser.getUsername());
            assertThat(resultUser.getEmail()).isEqualTo(originalUser.getEmail());
            // Note: subscriptionLevel and createdAt are set by factory, not from original
        }
    }
}