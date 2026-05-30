package com.restaurant.dao;

import com.restaurant.model.FoodItem;
import java.util.List;
import java.util.Optional;

public interface FoodItemDAO {
    List<FoodItem> getAllAvailableItems();
    Optional<FoodItem> getFoodItemById(int foodItemId);
    List<FoodItem> getItemsByCategory(String category);
    List<FoodItem> searchItemsByName(String namePattern);
    List<String> getAllCategories();
    FoodItem createFoodItem(FoodItem item);
    boolean updateFoodItem(FoodItem item);
    boolean setAvailability(int foodItemId, String availability);
    boolean deleteFoodItem(int foodItemId);
}