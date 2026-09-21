package com.example.demo.dto.response;

public class AdminStatsResponse {

    private long totalUsers;
    private long totalBuyers;
    private long totalSellers;
    private long totalAdmins;
    private long totalProperties;
    private long approvedProperties;
    private long pendingProperties;
    private long totalInquiries;
    private long bannedUsers;

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private AdminStatsResponse r = new AdminStatsResponse();

        public Builder totalUsers(long v)          { r.totalUsers = v; return this; }
        public Builder totalBuyers(long v)         { r.totalBuyers = v; return this; }
        public Builder totalSellers(long v)        { r.totalSellers = v; return this; }
        public Builder totalAdmins(long v)         { r.totalAdmins = v; return this; }
        public Builder totalProperties(long v)     { r.totalProperties = v; return this; }
        public Builder approvedProperties(long v)  { r.approvedProperties = v; return this; }
        public Builder pendingProperties(long v)   { r.pendingProperties = v; return this; }
        public Builder totalInquiries(long v)      { r.totalInquiries = v; return this; }
        public Builder bannedUsers(long v)         { r.bannedUsers = v; return this; }

        public AdminStatsResponse build() { return r; }
    }

    // Getters
    public long getTotalUsers()          { return totalUsers; }
    public long getTotalBuyers()         { return totalBuyers; }
    public long getTotalSellers()        { return totalSellers; }
    public long getTotalAdmins()         { return totalAdmins; }
    public long getTotalProperties()     { return totalProperties; }
    public long getApprovedProperties()  { return approvedProperties; }
    public long getPendingProperties()   { return pendingProperties; }
    public long getTotalInquiries()      { return totalInquiries; }
    public long getBannedUsers()         { return bannedUsers; }
}