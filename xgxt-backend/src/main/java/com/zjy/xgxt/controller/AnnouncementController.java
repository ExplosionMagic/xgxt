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

        // 使用 LambdaQueryWrapper 避免字段名拼写错误
        com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Announcement> wrapper =
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<>();

        // 生成的 SQL 逻辑： AND ( target_type = 'ALL' OR (target_type = 'MAJOR' AND target_value = ?) OR (target_type = 'CLASS' AND target_value = ?) )
        wrapper.and(w -> {
            // 默认所有人都能看全校通知
            w.eq(Announcement::getTargetType, "ALL");

            // 如果传了专业，追加 OR 条件
            if (org.springframework.util.StringUtils.hasText(major)) {
                w.or(orWrap -> orWrap.eq(Announcement::getTargetType, "MAJOR").eq(Announcement::getTargetValue, major));
            }

            // 如果传了班级，追加 OR 条件
            if (org.springframework.util.StringUtils.hasText(className)) {
                w.or(orWrap -> orWrap.eq(Announcement::getTargetType, "CLASS").eq(Announcement::getTargetValue, className));
            }
        });

        wrapper.orderByDesc(Announcement::getId);
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