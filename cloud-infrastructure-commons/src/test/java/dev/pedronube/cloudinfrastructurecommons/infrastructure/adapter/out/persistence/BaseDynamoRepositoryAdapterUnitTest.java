package dev.pedronube.cloudinfrastructurecommons.infrastructure.adapter.out.persistence;

import dev.pedronube.cloudinfrastructurecommons.infrastructure.adapter.out.persistence.entity.UserEntity;
import dev.pedronube.cloudinfrastructurecommons.infrastructure.adapter.out.persistence.entity.mapper.UserEntityFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.enhanced.dynamodb.*;

import software.amazon.awssdk.enhanced.dynamodb.model.PageIterable;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BaseDynamoRepositoryAdapter Unit Tests")
class BaseDynamoRepositoryAdapterUnitTest {

    @Mock
    private DynamoDbEnhancedClient mockClient;

    @Mock
    private DynamoDbTable<UserEntity> mockTable;

    private TestBaseDynamoRepositoryAdapter adapter;

    @BeforeEach
    void setUp() {
        when(mockClient.table(eq("serverless-back-table"), any(TableSchema.class)))
                .thenReturn(mockTable);
        adapter = new TestBaseDynamoRepositoryAdapter(mockClient);
    }

    @Nested
    @DisplayName("Constructor and initialization")
    class ConstructorTests {

        @Test
        @DisplayName("Should initialize table with correct name and schema")
        void shouldInitializeTableWithCorrectNameAndSchema() {
            // Given & When (done in setUp)

            // Then
            verify(mockClient).table(eq("serverless-back-table"), any(TableSchema.class));
            assertThat(adapter.getTable()).isEqualTo(mockTable);
        }
    }

    @Nested
    @DisplayName("Save operations")
    class SaveTests {

        @Test
        @DisplayName("Should save entity successfully")
        void shouldSaveEntitySuccessfully() {
            // Given
            UserEntity entity = UserEntityFactory.fromCognitoData("sub-123", "user", "user@example.com");

            // When
            adapter.save(entity);

            // Then
            verify(mockTable).putItem(entity);
        }

        @Test
        @DisplayName("Should handle null entity gracefully")
        void shouldHandleNullEntityGracefully() {
            // Given
            UserEntity entity = null;

            // When
            adapter.save(entity);

            // Then
            verify(mockTable).putItem((UserEntity) null);
        }
    }

    @Nested
    @DisplayName("Find operations")
    class FindTests {

        @Test
        @DisplayName("Should find entity by id successfully")
        void shouldFindEntityByIdSuccessfully() {
            // Given
            String id = "USER#sub-123";
            UserEntity expectedEntity = UserEntityFactory.fromCognitoData("sub-123", "user", "user@example.com");
            when(mockTable.getItem(any(Key.class))).thenReturn(expectedEntity);

            // When
            Optional<UserEntity> result = adapter.findById(id);

            // Then
            assertThat(result)
                    .isPresent()
                    .contains(expectedEntity);
            verify(mockTable).getItem(any(Key.class));
        }

        @Test
        @DisplayName("Should return empty when entity not found")
        void shouldReturnEmptyWhenEntityNotFound() {
            // Given
            String id = "USER#nonexistent";
            when(mockTable.getItem(any(Key.class))).thenReturn(null);

            // When
            Optional<UserEntity> result = adapter.findById(id);

            // Then
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should find entities by attribute")
        void shouldFindEntitiesByAttribute() {
            // Given
            String attributeName = "email";
            String value = "test@example.com";
            UserEntity entity1 = UserEntityFactory.fromCognitoData("sub-1", "user1", "test@example.com");
            UserEntity entity2 = UserEntityFactory.fromCognitoData("sub-2", "user2", "test@example.com");
            
            @SuppressWarnings("unchecked")
            PageIterable<UserEntity> mockPageIterable = mock(PageIterable.class);
            when(mockTable.scan(any(ScanEnhancedRequest.class))).thenReturn(mockPageIterable);
            when(mockPageIterable.items()).thenReturn(() -> List.of(entity1, entity2).iterator());

            // When
            List<UserEntity> result = adapter.findByAttribute(attributeName, value);

            // Then
            assertThat(result)
                    .hasSize(2)
                    .containsExactly(entity1, entity2);
            verify(mockTable).scan(any(ScanEnhancedRequest.class));
        }

        @Test
        @DisplayName("Should find entities with pagination")
        void shouldFindEntitiesWithPagination() {
            // Given
            String lastKey = "USER#sub-100";
            int limit = 10;
            UserEntity entity = UserEntityFactory.fromCognitoData("sub-101", "user101", "user101@example.com");
            
            @SuppressWarnings("unchecked")
            PageIterable<UserEntity> mockPageIterable = mock(PageIterable.class);
            when(mockTable.scan(any(ScanEnhancedRequest.class))).thenReturn(mockPageIterable);
            when(mockPageIterable.items()).thenReturn(() -> List.of(entity).iterator());

            // When
            List<UserEntity> result = adapter.findWithPagination(lastKey, limit);

            // Then
            assertThat(result).hasSize(1);
            assertThat(result.get(0)).isEqualTo(entity);
            verify(mockTable).scan(any(ScanEnhancedRequest.class));
        }

        @Test
        @DisplayName("Should handle pagination without lastKey")
        void shouldHandlePaginationWithoutLastKey() {
            // Given
            String lastKey = null;
            int limit = 5;
            
            @SuppressWarnings("unchecked")
            PageIterable<UserEntity> mockPageIterable = mock(PageIterable.class);
            when(mockTable.scan(any(ScanEnhancedRequest.class))).thenReturn(mockPageIterable);
            when(mockPageIterable.items()).thenReturn(() -> List.<UserEntity>of().iterator());

            // When
            List<UserEntity> result = adapter.findWithPagination(lastKey, limit);

            // Then
            assertThat(result).isEmpty();
            verify(mockTable).scan(any(ScanEnhancedRequest.class));
        }
    }

    @Nested
    @DisplayName("Delete operations")
    class DeleteTests {

        @Test
        @DisplayName("Should delete entity by id successfully")
        void shouldDeleteEntityByIdSuccessfully() {
            // Given
            String id = "USER#sub-123";

            // When
            adapter.deleteById(id);

            // Then
            verify(mockTable).deleteItem(any(Key.class));
        }

        @Test
        @DisplayName("Should handle delete of non-existent entity")
        void shouldHandleDeleteOfNonExistentEntity() {
            // Given
            String id = "USER#nonexistent";

            // When
            adapter.deleteById(id);

            // Then
            verify(mockTable).deleteItem(any(Key.class));
        }
    }

    // Test implementation of abstract class
    private static class TestBaseDynamoRepositoryAdapter extends BaseDynamoRepositoryAdapter<UserEntity> {
        
        public TestBaseDynamoRepositoryAdapter(DynamoDbEnhancedClient client) {
            super(client);
        }

        @Override
        protected TableSchema<UserEntity> getTableSchema() {
            return TableSchema.fromBean(UserEntity.class);
        }
        
        // Getter for testing
        public DynamoDbTable<UserEntity> getTable() {
            return this.table;
        }
    }
}