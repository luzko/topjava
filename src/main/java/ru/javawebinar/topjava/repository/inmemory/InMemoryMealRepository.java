package ru.javawebinar.topjava.repository.inmemory;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Repository;
import ru.javawebinar.topjava.model.Meal;
import ru.javawebinar.topjava.repository.MealRepository;
import ru.javawebinar.topjava.util.MealsUtil;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Repository
public class InMemoryMealRepository implements MealRepository {
    private final Map<Integer, Meal> repository = new ConcurrentHashMap<>();
    private final AtomicInteger counter = new AtomicInteger(0);

    {
        MealsUtil.meals.forEach(meal -> save(meal, 1));
    }

    @Override
    public Meal save(Meal meal, int userId) {
        if (meal.isNew()) {
            meal.setId(counter.incrementAndGet());
            meal.setUserId(userId);
            repository.put(meal.getId(), meal);
            return meal;
        }
        // handle case: update, but not present in storage
        Meal updateMeal = null;
        if (meal.getUserId() == userId) {
            updateMeal = repository.computeIfPresent(meal.getId(), (id, oldMeal) -> meal);
        }
        return updateMeal;
    }

    @Override
    public boolean delete(int id, int userId) {
        boolean isDelete = false;
        Meal meal = get(id, userId);
        if (meal.getUserId() == userId) {
            isDelete = repository.remove(id) != null;
        }
        return isDelete;
    }

    @Override
    public Meal get(int id, int userId) {
        Meal meal = repository.get(id);
        if (meal.getUserId() != userId) {
            meal = null;
        }
        return meal;
    }

    @Override
    public List<Meal> getAll(int userId) {
        return repository.values().stream()
            .filter(meal -> meal.getUserId() == userId)
            .sorted(Comparator.comparing(Meal::getDateTime))
            .collect(Collectors.toList());
    }
}
