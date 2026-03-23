package com.zjy.xgxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.GradeInfo;
import com.zjy.xgxt.service.GradeInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/grade")
@CrossOrigin
public class GradeInfoController {

    @Autowired
    private GradeInfoService gradeInfoService;

    @GetMapping("/list")
    public Result<List<GradeInfo>> list(@RequestParam(required = false) String gradeName) {
        LambdaQueryWrapper<GradeInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(gradeName)) {
            wrapper.like(GradeInfo::getGradeName, gradeName);
        }
        // 按年级名称降序排列，保证最新的年级在最前面
        wrapper.orderByDesc(GradeInfo::getGradeName);
        return Result.success(gradeInfoService.list(wrapper));
    }

    @PostMapping
    public Result<?> save(@RequestBody GradeInfo gradeInfo) {
        // 校验是否重复
        LambdaQueryWrapper<GradeInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(GradeInfo::getGradeName, gradeInfo.getGradeName());
        if (gradeInfoService.count(wrapper) > 0) {
            return Result.error("该年级已存在，请勿重复添加");
        }
        gradeInfo.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        gradeInfoService.save(gradeInfo);
        return Result.success("新增年级成功");
    }

    @PutMapping
    public Result<?> update(@RequestBody GradeInfo gradeInfo) {
        gradeInfoService.updateById(gradeInfo);
        return Result.success("修改年级成功");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        gradeInfoService.removeById(id);
        return Result.success("删除年级成功");
    }
}