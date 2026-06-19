package vn.qlcv.model;

public record Category(int id, String name, int userId) {
    @Override public String toString() { return name; }
}
