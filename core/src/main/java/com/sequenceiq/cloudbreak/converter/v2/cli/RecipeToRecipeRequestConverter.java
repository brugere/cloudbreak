package com.sequenceiq.cloudbreak.converter.v2.cli;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.sequenceiq.cloudbreak.api.model.RecipeRequest;
import com.sequenceiq.cloudbreak.converter.AbstractConversionServiceAwareConverter;
import com.sequenceiq.cloudbreak.domain.Recipe;

@Component
public class RecipeToRecipeRequestConverter
        extends AbstractConversionServiceAwareConverter<Recipe, RecipeRequest> {

    private static final Logger LOGGER = LoggerFactory.getLogger(RecipeToRecipeRequestConverter.class);

    @Override
    public RecipeRequest convert(Recipe source) {
        RecipeRequest recipeRequest = new RecipeRequest();
        recipeRequest.setName("");
        recipeRequest.setDescription(source.getDescription());
        recipeRequest.setRecipeType(source.getRecipeType());
        recipeRequest.setContent(source.getContent());
        return recipeRequest;
    }

}
