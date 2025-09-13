package dev.pedronube.cognitointegration.infrastructure.adapter.in.web.mapper;

import com.amazonaws.services.lambda.runtime.events.CognitoUserPoolPostConfirmationEvent;
import dev.pedronube.domaincommons.domain.model.user.User;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;

@NoArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public final class CognitoEventMapper {

    public static final Function<CognitoUserPoolPostConfirmationEvent, User> toDomain =
            event -> Optional.ofNullable(event)
                    .map(CognitoEventMapper::extractUser)
                    .orElse(null);

    private static User extractUser(CognitoUserPoolPostConfirmationEvent event) {
        return Optional.ofNullable(event)
                .filter(e -> e.getRequest() != null)
                .filter(e -> e.getRequest().getUserAttributes() != null)
                .map(e -> Optional.of(new User())
                        .map(user -> setBasicAttributes(user, e))
                        .map(CognitoEventMapper::setDefaults)
                        .orElseThrow())
                .orElse(null);
    }

    private static User setBasicAttributes(User user, CognitoUserPoolPostConfirmationEvent event) {
        Map<String, String> attributes = event.getRequest().getUserAttributes();
        user.setSub(attributes.get("sub"));
        user.setUsername(event.getUserName());
        user.setEmail(attributes.get("email"));
        return user;
    }

    private static User setDefaults(User user) {
        user.setSubscriptionLevel("FREE");
        user.setCreatedAt(Instant.now().toString());
        return user;
    }

}
