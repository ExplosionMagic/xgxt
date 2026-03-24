package com.zjy.xgxt.service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjy.xgxt.entity.DisciplinaryRecord;
import com.zjy.xgxt.mapper.DisciplinaryRecordMapper;
import org.springframework.stereotype.Service;

@Service
public class DisciplinaryRecordServiceImpl extends ServiceImpl<DisciplinaryRecordMapper, DisciplinaryRecord> implements DisciplinaryRecordService {}