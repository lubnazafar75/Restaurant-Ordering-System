package com.restaurant.restaurant.menu;

import java.util.List;

public class MenuService {

    private MenuDAO menuDAO = new MenuDAO();

    // Get all food items
    public List<FoodItem> getAllItems() {
        return menuDAO.getAllItems();
    }

    // Add a new food item
    public boolean addItem(FoodItem item) {
        if (item.getName() == null || item.getName().isEmpty()) {
            System.out.println("Error: Food name cannot be empty");
            return false;
        }
        if (item.getPrice() <= 0) {
            System.out.println("Error: Price must be greater than zero");
            return false;
        }
        menuDAO.addItem(item);
        return true;
    }

    // Update an existing food item
    public boolean updateItem(FoodItem item) {
        if (item.getName() == null || item.getName().isEmpty()) {
            System.out.println("Error: Food name cannot be empty");
            return false;
        }
        if (item.getPrice() <= 0) {
            System.out.println("Error: Price must be greater than zero");
            return false;
        }
        menuDAO.updateItem(item);
        return true;
    }

    // Delete a food item
    public void deleteItem(int id) {
        menuDAO.deleteItem(id);
    }

    // Search food items
    public List<FoodItem> searchItems(String keyword) {
        return menuDAO.searchItems(keyword);
    }
}