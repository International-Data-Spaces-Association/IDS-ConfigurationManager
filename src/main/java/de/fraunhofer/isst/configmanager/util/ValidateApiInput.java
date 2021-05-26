package de.fraunhofer.isst.configmanager.util;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
@UtilityClass
public class ValidateApiInput {
    public static boolean notValid(final String... parameter) {
        if (log.isInfoEnabled()) {
            log.info("---- [ValidateApiInput] Validating API Input ...");
        }

        final var validationResult = Arrays.stream(parameter).dropWhile("undefined"::equals).count() == 0;

        if (validationResult) {
            if (log.isErrorEnabled()) {
                log.error("---- [ValidateApiInput] Validating API Input ... Input is NOT valid!");
            }
        } else {
            if (log.isInfoEnabled()) {
                log.info("---- [ValidateApiInput] Validating API Input ... Input is valid! ");
            }
        }

        return validationResult;
    }
}
