package com.zjy.xgxt.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjy.xgxt.entity.Major;
import com.zjy.xgxt.mapper.MajorMapper;
import org.springframework.stereotype.Service;

@Service
public class MajorServiceImpl extends ServiceImpl<MajorMapper, Major> implements MajorService {}
