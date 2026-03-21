package com.zjy.xgxt.service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjy.xgxt.entity.LeaveInfo;
import com.zjy.xgxt.mapper.LeaveInfoMapper;
import org.springframework.stereotype.Service;

@Service
public class LeaveInfoServiceImpl extends ServiceImpl<LeaveInfoMapper, LeaveInfo> implements LeaveInfoService {}