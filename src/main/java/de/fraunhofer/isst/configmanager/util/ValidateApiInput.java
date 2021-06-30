/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
