package com.restaurant.restaurant.model;
public class SalesData {
    private String date;
    private double totalRevenue;
    private int totalOrders;
    private int totalCustomers;
    private double averageOrderValue;
    private int peakHour;

    public SalesData() {}

    public SalesData(String date, double totalRevenue, int totalOrders, 
                     int totalCustomers, double averageOrderValue, int peakHour) {
        this.date = date;
        this.totalRevenue = totalRevenue;
        this.totalOrders = totalOrders;
        this.totalCustomers = totalCustomers;
        this.averageOrderValue = averageOrderValue;
        this.peakHour = peakHour;
    }

    // Getters and Setters
    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(double totalRevenue) { this.totalRevenue = totalRevenue; }

    public int getTotalOrders() { return totalOrders; }
    public void setTotalOrders(int totalOrders) { this.totalOrders = totalOrders; }

    public int getTotalCustomers() { return totalCustomers; }
    public void setTotalCustomers(int totalCustomers) { this.totalCustomers = totalCustomers; }

    public double getAverageOrderValue() { return averageOrderValue; }
    public void setAverageOrderValue(double averageOrderValue) { this.averageOrderValue = averageOrderValue; }

    public int getPeakHour() { return peakHour; }
    public void setPeakHour(int peakHour) { this.peakHour = peakHour; }
}