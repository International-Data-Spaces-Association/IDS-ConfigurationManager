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

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * This class can be used as a helper class to outsource program code here.
 */
@Slf4j
@UtilityClass
public class CalenderUtil {
    /**
     * This method creates an XMLGreorgianCalender for for time specifications as created or
     * modified.
     *
     * @return XMLGregorianCalender
     */
    public static XMLGregorianCalendar getGregorianNow() {
        final var c = new GregorianCalendar();
        c.setTime(new Date());
        try {
            return DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException e) {
            log.error(e.getMessage(), e);
        }
        return null;
    }
}
