package de.fraunhofer.isst.configmanager.util;

import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class ValidateApiInput {
    public static boolean notValid(final String... parameter) {
        log.info("---- Validating API Input ...");
        final var validationResult = Arrays.stream(parameter).dropWhile(param -> param.equals("undefined")).count() == 0;

        if (validationResult) {
            log.error("---- Validating API Input ... Input is NOT valid!");
        } else {
            log.info("---- Validating API Input ... Input is valid! ");
        }

        return validationResult;
    }
}
