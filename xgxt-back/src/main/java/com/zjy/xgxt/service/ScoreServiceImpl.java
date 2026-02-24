package com.zjy.xgxt.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjy.xgxt.entity.Score;
import com.zjy.xgxt.mapper.ScoreMapper;
import com.zjy.xgxt.service.ScoreService;
import org.springframework.stereotype.Service;

@Service // <--- 千万别漏了这个注解
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, Score> implements ScoreService {
}