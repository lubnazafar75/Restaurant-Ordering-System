package com.restaurant.dao;

import com.restaurant.model.Table;
import java.util.List;
import java.util.Optional;

public interface TableDAO {
    List<Table> getAllTables();
    Optional<Table> getTableById(int tableId);
    Optional<Table> getTableByNumber(int tableNumber);
    List<Table> getAvailableTables();
    List<Table> getOccupiedTables();
    List<Table> getAvailableTablesByCapacity(int minCapacity);
    Optional<Integer> getCurrentOrderId(int tableId);
    Table createTable(Table table);
    boolean updateTable(Table table);
    boolean markTableAvailable(int tableId);
    boolean markTableOccupied(int tableId, int orderId);
}