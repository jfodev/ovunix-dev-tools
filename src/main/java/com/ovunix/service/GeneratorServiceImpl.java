package com.ovunix.service;

import com.ovunix.config.Generator;
import com.ovunix.domain.AbstractEntity;
import com.ovunix.utils.UniqueNumberGenerator;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

@Service
public class GeneratorServiceImpl implements IdGeneratorStrategy {
    @Override
    public void generate(AbstractEntity entity)  {
        Class<?> currentClass = entity.getClass();
        while (currentClass != null) {
            for (Field field : currentClass.getDeclaredFields()) {
                field.setAccessible(true);
                Annotation annotation = field.getAnnotation(Generator.class);
                if (annotation != null) {
                    entity.setId(UniqueNumberGenerator.generateTimestamp());
                }
            }
            currentClass = currentClass.getSuperclass();
        }
    }
}
