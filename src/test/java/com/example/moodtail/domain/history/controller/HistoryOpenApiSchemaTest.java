package com.example.moodtail.domain.history.controller;

import com.example.moodtail.domain.history.controller.docs.HistoryControllerDocs;
import com.example.moodtail.domain.history.dto.request.HistoryCreateRequest;
import com.example.moodtail.domain.history.dto.request.HistoryUpdateRequest;
import com.example.moodtail.global.config.security.auth.PrincipalDetails;
import io.swagger.v3.core.converter.ModelConverters;
import io.swagger.v3.oas.annotations.Parameter;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

class HistoryOpenApiSchemaTest {

    @Test
    void createRequestDocumentsCocktailIdConstraints() {
        io.swagger.v3.oas.models.media.Schema<?> cocktailIds = property(
                generatedSchema(HistoryCreateRequest.class),
                "cocktailIds"
        );

        assertThat(cocktailIds.getTypes()).contains("array");
        assertThat(cocktailIds.getMinItems()).isEqualTo(1);
        assertThat(cocktailIds.getUniqueItems()).isTrue();
        assertThat(cocktailIds.getItems().getMinimum()).isEqualByComparingTo("1");
    }

    @Test
    void updateRequestDocumentsRequiredChangeAndPositiveCocktailId() {
        io.swagger.v3.oas.models.media.Schema<?> request = generatedSchema(
                HistoryUpdateRequest.class
        );

        assertThat(request.getDescription()).contains("최소 하나");
        assertThat(property(request, "cocktailId").getMinimum()).isEqualByComparingTo("1");
    }

    @Test
    void photoUploadDocumentsDateAndImagePartTypes() throws Exception {
        Method method = HistoryControllerDocs.class.getDeclaredMethod(
                "addHistoryPhoto",
                PrincipalDetails.class,
                String.class,
                MultipartFile.class
        );

        Parameter date = method.getParameters()[1].getAnnotation(Parameter.class);
        Parameter image = method.getParameters()[2].getAnnotation(Parameter.class);

        assertThat(date.schema().types()).containsExactly("string");
        assertThat(date.schema().format()).isEqualTo("date");
        assertThat(image.schema().types()).containsExactly("string");
        assertThat(image.schema().format()).isEqualTo("binary");
    }

    private io.swagger.v3.oas.models.media.Schema<?> generatedSchema(Class<?> type) {
        return ModelConverters.getInstance(true)
                .readAll(type)
                .get(type.getSimpleName());
    }

    private io.swagger.v3.oas.models.media.Schema<?> property(
            io.swagger.v3.oas.models.media.Schema<?> schema,
            String name
    ) {
        return (io.swagger.v3.oas.models.media.Schema<?>) schema.getProperties().get(name);
    }
}
