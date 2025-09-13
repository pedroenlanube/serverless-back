package dev.pedronube.cloudinfrastructurecommons.infrastructure.adapter.out.persistence;

import dev.pedronube.domaincommons.domain.port.out.repository.QueryableRepositoryPort;
import software.amazon.awssdk.enhanced.dynamodb.*;
import software.amazon.awssdk.enhanced.dynamodb.model.ScanEnhancedRequest;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public abstract class BaseDynamoRepositoryAdapter<T> implements QueryableRepositoryPort<T> {

    protected final DynamoDbEnhancedClient client;
    protected DynamoDbTable<T> table;

    // TABLA ÚNICA para todos los adaptadores
    private static final String SINGLE_TABLE_NAME = "serverless-back-table";

    protected BaseDynamoRepositoryAdapter(DynamoDbEnhancedClient client) {
        this.client = client;
        this.table = client.table(SINGLE_TABLE_NAME, getTableSchema());
    }

    protected abstract TableSchema<T> getTableSchema();

    @Override
    public void save(T entity) {
        table.putItem(entity);
    }

    @Override
    public List<T> findByAttribute(String attributeName, String value) {
        return table.scan(ScanEnhancedRequest.builder()
                        .filterExpression(Expression.builder()
                                .expression("#attr = :val")
                                .putExpressionName("#attr", attributeName)
                                .putExpressionValue(":val", AttributeValue.builder().s(value).build())
                                .build())
                        .build())
                .items()
                .stream()
                .toList();
    }

    @Override
    public List<T> findWithPagination(String lastKey, int limit) {
        // Implementación básica - retorna primer item con paginación
        ScanEnhancedRequest request = ScanEnhancedRequest.builder()
                .limit(limit)
                .exclusiveStartKey(lastKey != null ?
                        Map.of("PK", AttributeValue.builder().s(lastKey).build()) : null)
                .build();

        return table.scan(request).items().stream().toList();
    }

    @Override
    public Optional<T> findById(String s) {
        Key key = Key.builder().partitionValue(s).build();
        return Optional.ofNullable(table.getItem(key));
    }

    @Override
    public void deleteById(String s) {
        Key key = Key.builder().partitionValue(s).build();
        table.deleteItem(key);
    }
}
