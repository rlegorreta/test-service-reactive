/* Copyright (c) 2023, LegoSoft Soluciones, S.C.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are not permitted.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 *  Neo4jIdTypeConverter.kt
 *
 *  Developed 2023 by LegoSoftSoluciones, S.C. www.legosoft.com.mx
 */
package com.ailegorreta.authservice.util;

import com.ailegorreta.commons.utils.HasLogger;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.springframework.core.convert.TypeDescriptor;
import org.springframework.core.convert.converter.GenericConverter;
import org.springframework.data.neo4j.core.schema.Id;

import java.util.*;

/**
 * Converter from Neo4j driver integer value to Neo4jId
 *
 * @project : auth-server
 * @author rlh
 * @date May 2023
 */
public class Neo4jIdTypeConverter implements GenericConverter, HasLogger {

    @Override
    public Set<ConvertiblePair> getConvertibleTypes() {
        Set<ConvertiblePair> convertiblePairs = new HashSet<>();

        convertiblePairs.add(new ConvertiblePair(org.neo4j.driver.internal.value.IntegerValue.class, Id.class));
        convertiblePairs.add(new ConvertiblePair(Id.class, org.neo4j.driver.internal.value.IntegerValue.class));

        return convertiblePairs;
    }

    @Override
    public Object convert(Object source, TypeDescriptor sourceType, TypeDescriptor targetType) {
        if (org.neo4j.driver.internal.value.IntegerValue.class.isAssignableFrom(sourceType.getType())) {
            // convert to Neo4j Driver Value

            return Neo4jId.of(source.toString());
        } else {
            // convert to ElementIdTypeConverter
            try {
                Neo4jId value = (Neo4jId) source;
                var longValue = Long.getLong(value.getValue());

                return new org.neo4j.driver.internal.value.IntegerValue(longValue);
            } catch (Exception e) {
                getLogger().error("Error en la conversión de org.neo4j.driver.internal.value.IntegerValue a ElementId");
                return new  org.neo4j.driver.internal.value.IntegerValue(-1l);
            }
        }
    }

    @NotNull
    @Override
    public Logger getLogger() { return HasLogger.DefaultImpls.getLogger(this); }

}