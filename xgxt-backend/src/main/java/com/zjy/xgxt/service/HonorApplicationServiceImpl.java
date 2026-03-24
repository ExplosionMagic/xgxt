package com.zjy.xgxt.service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjy.xgxt.entity.HonorApplication;
import com.zjy.xgxt.mapper.HonorApplicationMapper;
import org.springframework.stereotype.Service;

@Service
public class HonorApplicationServiceImpl extends ServiceImpl<HonorApplicationMapper, HonorApplication> implements HonorApplicationService {}