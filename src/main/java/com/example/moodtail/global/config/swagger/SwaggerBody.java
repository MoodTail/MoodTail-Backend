package com.example.moodtail.global.config.swagger;

import io.swagger.v3.oas.annotations.extensions.Extension;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@RequestBody
public @interface SwaggerBody {

    @AliasFor(annotation = RequestBody.class)
    String description() default "";

    @AliasFor(annotation = RequestBody.class)
    Content[] content() default {};

    @AliasFor(annotation = RequestBody.class)
    boolean required() default false;

    @AliasFor(annotation = RequestBody.class)
    Extension[] extensions() default {};

    @AliasFor(annotation = RequestBody.class)
    String ref() default "";
}
