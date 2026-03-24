package com.zjy.xgxt.service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjy.xgxt.entity.FinancialAid;
import com.zjy.xgxt.mapper.FinancialAidMapper;
import org.springframework.stereotype.Service;

@Service
public class FinancialAidServiceImpl extends ServiceImpl<FinancialAidMapper, FinancialAid> implements FinancialAidService {}