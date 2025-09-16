package dev.pedronube.cognitointegration.infrastructure.adapter.in.web;

import com.amazonaws.services.lambda.runtime.events.CognitoUserPoolPostConfirmationEvent;
import dev.pedronube.cognitointegration.infrastructure.adapter.in.web.mapper.CognitoEventMapper;
import dev.pedronube.domaincommons.domain.usecase.SaveUserUseCase;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Optional;
import java.util.function.Function;

@Slf4j
@Configuration
public class CognitoPostConfirmationService {

    @Bean
    public Function<CognitoUserPoolPostConfirmationEvent, CognitoUserPoolPostConfirmationEvent>
    postConfirmation(SaveUserUseCase saveUserUseCase) {
        return event -> Optional.ofNullable(event)
                .map(CognitoEventMapper.toDomain)
                .map(user -> {
                    log.info("Saving user: {}", user);
                    saveUserUseCase.accept(user);
                    return event;
                })
                .orElse(event);
    }

}
