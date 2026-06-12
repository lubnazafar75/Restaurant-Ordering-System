package com.restaurant.dao;

import com.restaurant.model.Staff;
import java.util.List;
import java.util.Optional;

public interface StaffDAO {
    Optional<Staff> authenticate(String username, String password);
    Optional<Staff> getStaffById(int staffId);
    List<Staff> getAllActiveStaff();
    List<Staff> getStaffByRole(String role);
    Staff createStaff(Staff staff);
    boolean updateStaff(Staff staff);
    boolean deactivateStaff(int staffId);
    boolean changePassword(int staffId, String newPassword);
}