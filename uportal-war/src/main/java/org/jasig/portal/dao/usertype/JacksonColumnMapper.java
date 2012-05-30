/**
 * Licensed to Jasig under one or more contributor license
 * agreements. See the NOTICE file distributed with this work
 * for additional information regarding copyright ownership.
 * Jasig licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a
 * copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.jasig.portal.dao.usertype;

import java.io.IOException;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectReader;
import org.codehaus.jackson.map.ObjectWriter;
import org.jadira.usertype.spi.shared.AbstractStringColumnMapper;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

/**
 * Mapper that read/writes objects to JSON
 * 
 * @author Eric Dalquist
 */
public class JacksonColumnMapper extends AbstractStringColumnMapper<Object> {
    private static final long serialVersionUID = 1L;
    
    private final LoadingCache<Class<?>, ObjectWriter> typedObjectWriters = CacheBuilder.newBuilder()
            .build(new CacheLoader<Class<?>, ObjectWriter>() {
                @Override
                public ObjectWriter load(Class<?> key) throws Exception {
                    return objectWriter.withType(key);
                }
            });
    private final LoadingCache<Class<?>, ObjectReader> typedObjectReaders = CacheBuilder.newBuilder()
            .build(new CacheLoader<Class<?>, ObjectReader>() {
                @Override
                public ObjectReader load(Class<?> key) throws Exception {
                    return objectReader.withType(key);
                }
            });
    
    private final ObjectWriter objectWriter;
    private final ObjectReader objectReader;
    
    public JacksonColumnMapper() {
        final ObjectMapper mapper = new ObjectMapper();

        customizeObjectMapper(mapper);
        
        this.objectWriter = this.createObjectWriter(mapper).withType(JsonWrapper.class);
        this.objectReader = this.createObjectReader(mapper).withType(JsonWrapper.class);
    }

    protected void customizeObjectMapper(ObjectMapper mapper) {
    }
    
    protected ObjectWriter createObjectWriter(ObjectMapper mapper) {
        return mapper.writer();
    }
    
    protected ObjectReader createObjectReader(ObjectMapper mapper) {
        return mapper.reader();
    }

    @Override
    public final Object fromNonNullValue(String s) {
        try {
            final JsonWrapper jsonWrapper = objectReader.readValue(s);
            final ObjectReader typeReader = typedObjectReaders.getUnchecked(jsonWrapper.getType());
            return typeReader.readValue(jsonWrapper.getValue());
        }
        catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Could not read from JSON: " + s, e);
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Could not read from JSON: " + s, e);
        }
    }

    @Override
    public final String toNonNullValue(Object value) {
        try {
            //Gross but 2-step is needed to deserialize using the correct type
            final Class<? extends Object> type = value.getClass();
            final ObjectWriter typeWriter = typedObjectWriters.getUnchecked(type);
            final String valueAsString = typeWriter.writeValueAsString(value);
            
            return objectWriter.writeValueAsString(new JsonWrapper(type, valueAsString));
        }
        catch (JsonGenerationException e) {
            throw new IllegalArgumentException("Could not write to JSON: " + value, e);
        }
        catch (JsonMappingException e) {
            throw new IllegalArgumentException("Could not write to JSON: " + value, e);
        }
        catch (IOException e) {
            throw new IllegalArgumentException("Could not write to JSON: " + value, e);
        }
    }
    
    public static final class JsonWrapper {
        private Class<?> type;
        private String value;
        
        public JsonWrapper() {
        }
        
        public JsonWrapper(Class<?> type, String value) {
            this.type = type;
            this.value = value;
        }
        
        public Class<?> getType() {
            return type;
        }
        public void setType(Class<?> type) {
            this.type = type;
        }
        public String getValue() {
            return value;
        }
        public void setValue(String value) {
            this.value = value;
        }
        @Override
        public String toString() {
            return "JsonWrapper [type=" + type + ", value=" + value + "]";
        }
    }
}