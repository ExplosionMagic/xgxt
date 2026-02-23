package com.zjy.xgxt.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjy.xgxt.entity.Student;
import com.zjy.xgxt.mapper.StudentMapper;
import com.zjy.xgxt.service.StudentService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    @Override
    public List<Student> listByName(String name) {
        QueryWrapper<Student> wrapper = new QueryWrapper<>();
        // 动态SQL：如果name不为空，则拼接like查询
        if (StringUtils.hasText(name)) {
            wrapper.like("name", name);
        }
        wrapper.orderByDesc("id"); // 默认按ID倒序
        return this.list(wrapper);
    }
}
