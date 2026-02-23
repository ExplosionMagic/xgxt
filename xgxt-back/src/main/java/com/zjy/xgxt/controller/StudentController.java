package com.zjy.xgxt.controller;

import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.Student;
import com.zjy.xgxt.service.StudentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/student")
@CrossOrigin
public class StudentController {

    @Autowired
    private StudentService studentService;

    // 查询
    @GetMapping("/list")
    public Result<List<Student>> list(@RequestParam(required = false) String name) {
        return Result.success(studentService.listByName(name));
    }

    // 新增
    @PostMapping
    public Result<?> save(@RequestBody Student student) {
        // IService 自带 save 方法
        studentService.save(student);
        return Result.success(null);
    }

    // 修改
    @PutMapping
    public Result<?> update(@RequestBody Student student) {
        // IService 自带 updateById 方法
        studentService.updateById(student);
        return Result.success(null);
    }

    // 删除
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Long id) {
        // IService 自带 removeById 方法
        studentService.removeById(id);
        return Result.success(null);
    }
}
