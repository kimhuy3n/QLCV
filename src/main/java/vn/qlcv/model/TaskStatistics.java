package vn.qlcv.model;

public record TaskStatistics(int total, int completed, int pending, int overdue) {
    public double completionRate() { return total == 0 ? 0 : completed * 100.0 / total; }
}
