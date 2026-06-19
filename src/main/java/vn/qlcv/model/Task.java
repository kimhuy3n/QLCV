package vn.qlcv.model;

import java.time.LocalDate;

public record Task(int id, String title, String description, LocalDate createdDate,
                   LocalDate deadline, String status, Integer categoryId,
                   String categoryName, int userId) {
    public boolean completed() { return "COMPLETED".equals(status); }
}
