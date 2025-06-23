package com.ovunix.core.service;

import com.ovunix.core.config.KeyGenerator;
import com.ovunix.core.domain.Persistable;
import com.ovunix.core.strategy.IdGeneratorStrategy;
import com.ovunix.core.utils.UniqueNumberGenerator;
import org.springframework.stereotype.Service;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;

@Service
public class GeneratorServiceImpl implements IdGeneratorStrategy {

    @Override
    public void generate(Persistable entity) {
        Class<?> currentClass = entity.getClass();

        while (currentClass != null) {
            for (Field field : currentClass.getDeclaredFields()) {
                if (field.isAnnotationPresent(KeyGenerator.class)) {
                    try {
                        field.setAccessible(true);

                        // Ne génère l'ID que si null (évite d’écraser une valeur existante)
                        Object value = field.get(entity);
                        if (value == null) {
                            String generatedId = UniqueNumberGenerator.generateTimestamp();
                            field.set(entity, generatedId);
                            entity.setId(generatedId); // Met à jour l'identifiant principal
                        }

                        return; // on arrête après le premier champ annoté
                    } catch (IllegalAccessException e) {
                        throw new RuntimeException("Failed to generate ID for field: " + field.getName(), e);
                    }
                }
            }
            currentClass = currentClass.getSuperclass();
        }
    }
}
