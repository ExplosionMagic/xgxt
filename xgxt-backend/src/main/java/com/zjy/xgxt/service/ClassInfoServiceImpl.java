package com.zjy.xgxt.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjy.xgxt.entity.ClassInfo;
import com.zjy.xgxt.mapper.ClassInfoMapper;
import org.springframework.stereotype.Service;


@Service
public class ClassInfoServiceImpl extends ServiceImpl<ClassInfoMapper, ClassInfo> implements ClassInfoService {}
