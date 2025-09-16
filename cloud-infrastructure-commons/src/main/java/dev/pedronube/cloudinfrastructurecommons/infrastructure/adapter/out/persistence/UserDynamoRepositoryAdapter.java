package dev.pedronube.cloudinfrastructurecommons.infrastructure.adapter.out.persistence;

import dev.pedronube.cloudinfrastructurecommons.infrastructure.adapter.out.persistence.entity.UserEntity;
import dev.pedronube.cloudinfrastructurecommons.infrastructure.adapter.out.persistence.mapper.UserMapper;
import dev.pedronube.domaincommons.domain.port.out.repository.user.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.enhanced.dynamodb.DynamoDbEnhancedClient;
import software.amazon.awssdk.enhanced.dynamodb.Key;
import software.amazon.awssdk.enhanced.dynamodb.TableSchema;

import java.util.Optional;

@Configuration
public class UserDynamoRepositoryAdapter extends BaseDynamoRepositoryAdapter<UserEntity> {

    public UserDynamoRepositoryAdapter(DynamoDbEnhancedClient client) {
        super(client);
    }

    @Override
    protected TableSchema<UserEntity> getTableSchema() {
        return TableSchema.fromBean(UserEntity.class);
    }

    @Bean
    public SaveUserPort saveUserPort() {
        return user -> save(UserMapper.toEntity.apply(user));
    }

    @Bean
    public FindUserByIdPort findUserByIdPort() {
        return sub -> findUserBySub(sub)
                .map(UserMapper.toDomain);
    }

    @Bean
    public FindUserByEmailPort findUserByEmailPort() {
        return email -> findByAttribute("email", email)
                .stream()
                .findFirst()
                .map(UserMapper.toDomain);
    }

    @Bean
    public UpdateUserPort updateUserPort() {
        return user -> save(UserMapper.toEntity.apply(user));
    }

    @Bean
    public DeleteUserPort deleteUserPort() {
        return sub -> deleteById("USER#" + sub);
    }

    private Optional<UserEntity> findUserBySub(String sub) {
        Key key = Key.builder()
                .partitionValue("USER#" + sub)
                .sortValue("PROFILE")
                .build();
        return Optional.ofNullable(table.getItem(key));
    }
}

