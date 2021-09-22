package org.learn.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.core.type.ClassMetadata;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.core.type.filter.TypeFilter;

import java.util.HashSet;
import java.util.Set;

@Slf4j
public class ExcludeOuterConfigFilter implements TypeFilter {

    private static final Set<String> EXCLUDE_OUTER_CONFIG_CLASS = new HashSet<>();

    static {
        EXCLUDE_OUTER_CONFIG_CLASS.add("com.sdk.rest.DispatchFeignCloudConfig");
    }

    @Override
    public boolean match(MetadataReader metadataReader, MetadataReaderFactory metadataReaderFactory) {

        // 当前被扫描类的注解信息
        AnnotationMetadata annotationMetadata = metadataReader.getAnnotationMetadata();
        // 当前被扫描类信息
        ClassMetadata classMetadata = metadataReader.getClassMetadata();
        // 当前被扫描类资源信息
        Resource resource = metadataReader.getResource();
        String className = classMetadata.getClassName();
        //Class<?> forName = Class.forName(className); .class.isAssignableFrom(forName)

        if (EXCLUDE_OUTER_CONFIG_CLASS.contains(className)) {
            log.info("ingore outer config className : {}", className);
            return true;
        }
        return false;
    }
}
