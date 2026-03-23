package com.zjy.xgxt.service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjy.xgxt.entity.GradeInfo;
import com.zjy.xgxt.mapper.GradeInfoMapper;
import org.springframework.stereotype.Service;

@Service
public class GradeInfoServiceImpl extends ServiceImpl<GradeInfoMapper, GradeInfo> implements GradeInfoService {}