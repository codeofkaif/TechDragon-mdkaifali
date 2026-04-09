package com.airasoi.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "dishes")
public class Dish {
    @Id
    private UUID id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 30)
    private String cuisine;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "dish_ingredients", joinColumns = @JoinColumn(name = "dish_id"))
    @Column(name = "ingredient", nullable = false, length = 50)
    private List<String> ingredients = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt;

    protected Dish() {}

    public Dish(UUID id, String name, String cuisine, List<String> ingredients, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.cuisine = cuisine;
        this.ingredients = new ArrayList<>(ingredients);
        this.createdAt = createdAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCuisine() {
        return cuisine;
    }

    public List<String> getIngredients() {
        return List.copyOf(ingredients);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}

