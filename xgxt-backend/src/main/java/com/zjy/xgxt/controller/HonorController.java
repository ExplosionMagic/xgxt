package com.zjy.xgxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.HonorApplication;
import com.zjy.xgxt.service.HonorApplicationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/honor")
@CrossOrigin
public class HonorController {

    @Autowired
    private HonorApplicationService honorService;

    @GetMapping("/list")
    public Result<List<HonorApplication>> list(
            @RequestParam String role,
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<HonorApplication> wrapper = new LambdaQueryWrapper<>();

        if ("STUDENT".equals(role)) {
            wrapper.eq(HonorApplication::getStudentNo, studentNo);
        } else {
            if (status != null) {
                wrapper.eq(HonorApplication::getStatus, status);
            }
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(HonorApplication::getStudentName, keyword)
                        .or()
                        .like(HonorApplication::getStudentNo, keyword)
                        .or()
                        .like(HonorApplication::getHonorName, keyword));
            }
        }

        wrapper.orderByDesc(HonorApplication::getId);
        return Result.success(honorService.list(wrapper));
    }

    @PostMapping("/apply")
    public Result<?> apply(@RequestBody HonorApplication honor) {
        // 校验：同一奖项是否重复申请
        LambdaQueryWrapper<HonorApplication> check = new LambdaQueryWrapper<>();
        check.eq(HonorApplication::getStudentNo, honor.getStudentNo())
                .eq(HonorApplication::getHonorName, honor.getHonorName())
                .in(HonorApplication::getStatus, 0, 2, 4);
        if (honorService.count(check) > 0) {
            return Result.error("您已有正在申请的荣誉奖项或该荣誉奖项已通过申请，无法重复申请");
        }

        honor.setStatus(0);
        honor.setApplyTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        honorService.save(honor);
        return Result.success("已提交荣誉奖项申请，等待初审");
    }

    @PutMapping("/audit/teacher")
    public Result<?> teacherAudit(@RequestBody HonorApplication honor) {
        honorService.updateById(honor);
        return Result.success(honor.getStatus() == 2 ? "初审通过" : "已驳回申请");
    }

    @PutMapping("/audit/admin")
    public Result<?> adminAudit(@RequestBody HonorApplication honor) {
        honorService.updateById(honor);
        return Result.success(honor.getStatus() == 4 ? "终审通过" : "已驳回申请");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        honorService.removeById(id);
        return Result.success("申请已撤销");
    }
}