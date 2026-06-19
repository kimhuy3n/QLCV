package vn.qlcv.controller;

import vn.qlcv.dao.CategoryDAO;
import vn.qlcv.dao.TaskDAO;
import vn.qlcv.model.*;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class TaskController {
    private final TaskDAO tasks = new TaskDAO();
    private final CategoryDAO categories = new CategoryDAO();

    public List<Task> search(int userId,String keyword,String status,Integer categoryId)throws SQLException{return tasks.search(userId,keyword,status,categoryId);}
    public List<Category> categories(int userId)throws SQLException{return categories.findAll(userId);}
    public TaskStatistics statistics(int userId)throws SQLException{return tasks.statistics(userId);}
    public void save(Task task)throws SQLException{validate(task);if(task.id()==0)tasks.insert(task);else tasks.update(task);}
    public void delete(int id,int userId)throws SQLException{tasks.delete(id,userId);}
    public void complete(int id,int userId)throws SQLException{tasks.markCompleted(id,userId);}
    public void reopen(int id,int userId)throws SQLException{tasks.markPending(id,userId);}
    public void changeStatus(int id,int userId,String status)throws SQLException{
        if(status==null||status.isBlank()||!java.util.Set.of("PENDING","IN_PROGRESS","COMPLETED").contains(status))
            throw new IllegalArgumentException("Trạng thái không hợp lệ.");
        tasks.changeStatus(id,userId,status);
    }
    public void addCategory(String name,int userId)throws SQLException{checkName(name);categories.insert(name.trim(),userId);}
    public void updateCategory(int id,String name,int userId)throws SQLException{checkName(name);categories.update(id,name.trim(),userId);}
    public void deleteCategory(int id,int userId)throws SQLException{categories.delete(id,userId);}
    private void validate(Task t){
        if(t.title()==null||t.title().isBlank())throw new IllegalArgumentException("Tên công việc không được để trống.");
        if(t.deadline()==null)throw new IllegalArgumentException("Deadline không hợp lệ (yyyy-MM-dd).");
        if(t.status()==null||t.status().isBlank())throw new IllegalArgumentException("Trạng thái không hợp lệ.");
        if(!java.util.Set.of("PENDING","IN_PROGRESS","COMPLETED").contains(t.status()))
            throw new IllegalArgumentException("Trạng thái không hợp lệ.");
    }
    private void checkName(String name){if(name==null||name.isBlank())throw new IllegalArgumentException("Tên danh mục không được để trống.");}
}
