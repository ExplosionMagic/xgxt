package com.zjy.xgxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.Major;
import com.zjy.xgxt.service.MajorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/major")
@CrossOrigin // 允许跨域
public class MajorController {

    @Autowired
    private MajorService majorService;

    /**
     * 获取所有专业列表 (支持按专业名模糊搜索)
     */
    @GetMapping("/list")
    public Result<List<Major>> list(@RequestParam(required = false) String majorName) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(majorName)) {
            wrapper.like(Major::getMajorName, majorName);
        }
        // 按ID倒序，新添加的排在前面
        wrapper.orderByDesc(Major::getId);
        return Result.success(majorService.list(wrapper));
    }

    /**
     * 新增专业
     */
    @PostMapping
    public Result<?> save(@RequestBody Major major) {
        // 校验专业名是否重复
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Major::getMajorName, major.getMajorName());
        if (majorService.count(wrapper) > 0) {
            return Result.error("该专业名称已存在");
        }
        majorService.save(major);
        return Result.success("新增专业成功");
    }

    /**
     * 修改专业
     */
    @PutMapping
    public Result<?> update(@RequestBody Major major) {
        if (major.getId() == null) {
            return Result.error("专业ID不能为空");
        }
        majorService.updateById(major);
        return Result.success("修改专业成功");
    }

    /**
     * 删除专业
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        majorService.removeById(id);
        return Result.success("删除专业成功");
    }
}
