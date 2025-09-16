package dev.pedronube.cloudinfrastructurecommons.infrastructure.adapter.out.persistence.entity.mapper;

import dev.pedronube.cloudinfrastructurecommons.infrastructure.adapter.out.persistence.entity.UserEntity;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;

@Slf4j
@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class UserEntityFactory {

    public static UserEntity fromCognitoData(String sub, String username, String email) {
        // Usar el patrón Builder que añadimos a la entidad es mucho más legible
        UserEntity entity = UserEntity.builder()
                .sub(sub)
                .username(username)
                .email(email)
                .subscriptionLevel("FREE") // Lógica de valor por defecto
                .createdAt(Instant.now().toString())
                .build();

        // La lógica de las claves de persistencia pertenece aquí,
        // en la capa de adaptación a la infraestructura.
        entity.setPk("USER#" + sub);
        entity.setSk("PROFILE");
        log.info("[UserEntityFactory_mayus] Creada entidad de usuario: {}", entity);
        return entity;
    }
}