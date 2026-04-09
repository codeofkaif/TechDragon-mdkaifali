package com.airasoi.service;

import com.airasoi.dto.DishDto;
import com.airasoi.dto.GenerateDishesRequest;
import com.airasoi.entity.Dish;
import com.airasoi.repository.DishRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

@Service
public class DishGeneratorService {
    private static final List<String> STYLES = List.of(
            "Stir-Fry",
            "Curry",
            "Tacos",
            "Salad",
            "Bowl",
            "Pasta",
            "Soup",
            "Wrap",
            "Skillet",
            "Bake"
    );

    private final DishRepository dishRepository;
    private final Clock clock;
    private final SecureRandom random;

    @Autowired
    public DishGeneratorService(DishRepository dishRepository) {
        this(dishRepository, Clock.systemUTC(), new SecureRandom());
    }

    private DishGeneratorService(DishRepository dishRepository, Clock clock, SecureRandom random) {
        this.dishRepository = dishRepository;
        this.clock = clock;
        this.random = random;
    }

    @Transactional
    public List<DishDto> generate(GenerateDishesRequest request) {
        List<String> cleanedIngredients = request.ingredients().stream()
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.toLowerCase(Locale.ROOT))
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();

        String cuisine = normalizeCuisine(request.cuisine());
        int count = request.resolvedCount();

        Instant now = Instant.now(clock);
        List<Dish> dishesToSave = new ArrayList<>(count);

        for (int i = 0; i < count; i++) {
            String main = pickMain(cleanedIngredients);
            String style = STYLES.get(random.nextInt(STYLES.size()));
            String name = buildName(cuisine, main, style);

            dishesToSave.add(new Dish(
                    UUID.randomUUID(),
                    name,
                    cuisine,
                    cleanedIngredients,
                    now
            ));
        }

        return dishRepository.saveAll(dishesToSave).stream()
                .map(d -> new DishDto(d.getId(), d.getName(), d.getCuisine(), d.getIngredients()))
                .toList();
    }

    private String normalizeCuisine(String cuisine) {
        if (cuisine == null) return null;
        String trimmed = cuisine.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String pickMain(List<String> ingredients) {
        if (ingredients.isEmpty()) return "Chef's Choice";
        return ingredients.get(random.nextInt(ingredients.size()));
    }

    private String buildName(String cuisine, String main, String style) {
        String mainTitle = titleCase(main);
        if (cuisine == null) return mainTitle + " " + style;
        return cuisine.trim() + " " + mainTitle + " " + style;
    }

    private String titleCase(String s) {
        String trimmed = s.trim();
        if (trimmed.isEmpty()) return trimmed;
        if (trimmed.length() == 1) return trimmed.toUpperCase(Locale.ROOT);
        return trimmed.substring(0, 1).toUpperCase(Locale.ROOT) + trimmed.substring(1);
    }
}

