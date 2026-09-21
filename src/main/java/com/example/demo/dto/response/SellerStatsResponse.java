package com.example.demo.dto.response;

public class SellerStatsResponse {

    private long totalListings;
    private long approvedListings;
    private long pendingListings;
    private long totalViews;
    private long totalInquiries;

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private SellerStatsResponse r = new SellerStatsResponse();

        public Builder totalListings(long v)    { r.totalListings = v; return this; }
        public Builder approvedListings(long v) { r.approvedListings = v; return this; }
        public Builder pendingListings(long v)  { r.pendingListings = v; return this; }
        public Builder totalViews(long v)       { r.totalViews = v; return this; }
        public Builder totalInquiries(long v)   { r.totalInquiries = v; return this; }

        public SellerStatsResponse build() { return r; }
    }

    public long getTotalListings()    { return totalListings; }
    public long getApprovedListings() { return approvedListings; }
    public long getPendingListings()  { return pendingListings; }
    public long getTotalViews()       { return totalViews; }
    public long getTotalInquiries()   { return totalInquiries; }
}