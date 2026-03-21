package com.zjy.xgxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.Announcement;
import com.zjy.xgxt.service.AnnouncementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/announcement")
@CrossOrigin
public class AnnouncementController {

    @Autowired
    private AnnouncementService announcementService;

    /**
     * 1. 管理员获取所有公告列表
     */
    @GetMapping("/list")
    public Result<List<Announcement>> list(@RequestParam(required = false) String title) {
        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();
        if (StringUtils.hasText(title)) {
            wrapper.like("title", title);
        }
        wrapper.orderByDesc("id");
        return Result.success(announcementService.list(wrapper));
    }

    /**
     * 2. 学生/教师获取自己能看的公告 (首页展示用)
     * 逻辑：全校通知必看 + 自己专业的通知 + 自己班级的通知
     */
    @GetMapping("/my")
    public Result<List<Announcement>> getMyAnnouncements(
            @RequestParam String role,
            @RequestParam(required = false) String major,
            @RequestParam(required = false) String className) {

        QueryWrapper<Announcement> wrapper = new QueryWrapper<>();

        // 使用 SQL 的 OR 逻辑来过滤权限
        wrapper.and(w -> {
            w.eq("target_type", "ALL"); // 所有人都能看全校通知

            if ("STUDENT".equals(role)) {
                if (StringUtils.hasText(major)) {
                    w.or(orWrap -> orWrap.eq("target_type", "MAJOR").eq("target_value", major));
                }
                if (StringUtils.hasText(className)) {
                    w.or(orWrap -> orWrap.eq("target_type", "CLASS").eq("target_value", className));
                }
            } else if ("TEACHER".equals(role)) {
                // 假设教师也能看自己所在专业的通知
                if (StringUtils.hasText(major)) {
                    w.or(orWrap -> orWrap.eq("target_type", "MAJOR").eq("target_value", major));
                }
            }
        });

        wrapper.orderByDesc("id");
        return Result.success(announcementService.list(wrapper));
    }

    /**
     * 3. 发布/修改公告
     */
    @PostMapping
    public Result<?> saveOrUpdate(@RequestBody Announcement announcement) {
        if (announcement.getId() == null) {
            // 新增时自动注入当前时间
            announcement.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        }
        announcementService.saveOrUpdate(announcement);
        return Result.success("公告发布成功");
    }

    /**
     * 4. 删除公告
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        announcementService.removeById(id);
        return Result.success("公告删除成功");
    }
}