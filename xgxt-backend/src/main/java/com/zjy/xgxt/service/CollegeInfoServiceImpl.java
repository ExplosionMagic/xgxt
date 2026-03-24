package com.zjy.xgxt.service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjy.xgxt.entity.CollegeInfo;
import com.zjy.xgxt.mapper.CollegeInfoMapper;
import org.springframework.stereotype.Service;

@Service
public class CollegeInfoServiceImpl extends ServiceImpl<CollegeInfoMapper, CollegeInfo> implements CollegeInfoService {}