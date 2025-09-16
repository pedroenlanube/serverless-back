package dev.pedronube.cloudinfrastructurecommons.infrastructure.adapter.out.persistence.mapper;

import dev.pedronube.cloudinfrastructurecommons.infrastructure.adapter.out.persistence.entity.UserEntity;
import dev.pedronube.cloudinfrastructurecommons.infrastructure.adapter.out.persistence.entity.mapper.UserEntityFactory;
import dev.pedronube.domaincommons.domain.model.user.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {

    public static final Function<UserEntity, User> toDomain = entity ->
            Optional.ofNullable(entity)
                    .map(UserMapper::createDomainUser)
                    .orElse(null);


    public static final Function<User, UserEntity> toEntity = user ->
            Optional.ofNullable(user)
                    .map(UserMapper::createEntityUser)
                    .orElse(null);

    private static User createDomainUser(UserEntity entity) {
        User user = new User();
        user.setSub(entity.getSub());
        user.setUsername(entity.getUsername());
        user.setEmail(entity.getEmail());
        user.setSubscriptionLevel(entity.getSubscriptionLevel());
        user.setCreatedAt(entity.getCreatedAt());
        return user;
    }

    private static UserEntity createEntityUser(User user) {
        return UserEntityFactory.fromCognitoData(
                user.getSub(),
                user.getUsername(),
                user.getEmail()
        );
    }
}