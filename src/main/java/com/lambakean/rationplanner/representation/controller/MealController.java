package com.lambakean.rationplanner.representation.controller;

import com.lambakean.rationplanner.representation.dto.MealDto;
import com.lambakean.rationplanner.representation.dto.form.MealCreationForm;
import com.lambakean.rationplanner.domain.mapper.MealMapper;
import com.lambakean.rationplanner.data.model.Meal;
import com.lambakean.rationplanner.domain.service.MealService;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/meal")
@RequiredArgsConstructor
public class MealController {

    private final MealService mealService;
    private final MealMapper mealMapper;

    @ApiOperation("Создание блюда")
    @PostMapping
    public ResponseEntity<MealDto> createMeal(@RequestBody MealCreationForm mealCreationForm) {

        Meal createdMeal = mealService.createMeal(mealMapper.toMeal(mealCreationForm));
        MealDto createdMealDto = mealMapper.toMealDto(createdMeal);

        return ResponseEntity.ok(createdMealDto);
    }

    @ApiOperation("Получение списка блюд текущего пользователя")
    @GetMapping
    public ResponseEntity<List<MealDto>> getMeals() {

        List<Meal> currentUserMeals = mealService.getCurrentUserMeals();

        return ResponseEntity.ok(
                currentUserMeals
                        .stream()
                        .map(mealMapper::toMealDto)
                        .collect(Collectors.toList())
        );
    }

    @ApiOperation("Получение информации о блюде по его id")
    @GetMapping("/{id}")
    public ResponseEntity<MealDto> getMealById(@PathVariable String id) {

        Meal meal = mealService.getMealById(id);

        return ResponseEntity.ok(mealMapper.toMealDto(meal));
    }

    @ApiOperation("Удаление блюда по его id")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMealById(@PathVariable String id) {

        mealService.deleteMealById(id);

        return ResponseEntity.noContent().build();
    }
}
