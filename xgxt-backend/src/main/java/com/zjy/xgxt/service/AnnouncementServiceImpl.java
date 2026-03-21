package com.zjy.xgxt.service;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjy.xgxt.entity.Announcement;
import com.zjy.xgxt.mapper.AnnouncementMapper;
import org.springframework.stereotype.Service;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements AnnouncementService {}