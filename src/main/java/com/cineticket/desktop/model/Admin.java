package com.cineticket.desktop.model;

public final class Admin extends User {
    private final String adminId;

    public Admin(String userId, String username, String passwordHash,
                 String fullName, String email, boolean active) {
        super(userId, username, passwordHash, fullName, email, active);
        this.adminId = userId;
    }

    public void manageUsers(User user, boolean active) { user.setActive(active); }
    public void manageStaffAccounts(Staff staff, boolean active) { staff.setActive(active); }
    public RevenueReport viewRevenueReport(RevenueReport report) { return report.generateReport(); }
    public String getAdminId() { return adminId; }
    @Override public UserRole getRole() { return UserRole.ADMIN; }
}
