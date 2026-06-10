package com.restaurant.dao;

import com.restaurant.model.SalesData;
import java.util.List;
import java.util.Optional;

/**
 * SalesDataDAO - Handles analytics and reporting
 * Responsible for: sales aggregates, revenue reports, analytics
 */
public interface SalesDataDAO {
    
    /**
     * Get or create sales record for today
     */
    SalesData getTodaysSalesData();
    
    /**
     * Get sales data for specific date
     */
    Optional<SalesData> getSalesDataByDate(String date);
    
    /**
     * Get sales for date range
     */
    List<SalesData> getSalesDataRange(String startDate, String endDate);
    
    /**
     * Update daily sales totals (call after order completion)
     */
    boolean updateDailySales(String date);
    
    /**
     * Get total revenue for today
     */
    double getTodayRevenue();
    
    /**
     * Get total revenue for week
     */
    double getWeeklyRevenue();
    
    /**
     * Get total revenue for month
     */
    double getMonthlyRevenue();
    
    /**
     * Get order count for today
     */
    int getTodayOrderCount();
    
    /**
     * Get average order value for today
     */
    double getTodayAverageOrderValue();
    
    /**
     * Get peak hour (hour with most orders)
     */
    Optional<Integer> getPeakHour(String date);
    
    /**
     * Get customer count for today
     */
    int getTodayCustomerCount();
    
    /**
     * Get occupancy rate (occupied tables / total tables)
     */
    double getCurrentOccupancyRate();
}