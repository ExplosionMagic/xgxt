package com.zjy.xgxt.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zjy.xgxt.entity.Student;
import java.util.List;

public interface StudentService extends IService<Student> {

    List<Student> listByName(String name);
}
