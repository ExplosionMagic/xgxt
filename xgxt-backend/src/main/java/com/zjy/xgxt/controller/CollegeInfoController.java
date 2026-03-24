package com.zjy.xgxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.CollegeInfo;
import com.zjy.xgxt.service.CollegeInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/college")
@CrossOrigin
public class CollegeInfoController {

    @Autowired
    private CollegeInfoService collegeInfoService;
    @Autowired
    private com.zjy.xgxt.service.MajorService majorService;

    @GetMapping("/list")
    public Result<List<CollegeInfo>> list(@RequestParam(required = false) String collegeName) {
        LambdaQueryWrapper<CollegeInfo> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(collegeName)) {
            wrapper.like(CollegeInfo::getCollegeName, collegeName);
        }
        wrapper.orderByAsc(CollegeInfo::getId);
        return Result.success(collegeInfoService.list(wrapper));
    }

    @PostMapping
    public Result<?> save(@RequestBody CollegeInfo collegeInfo) {
        LambdaQueryWrapper<CollegeInfo> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CollegeInfo::getCollegeName, collegeInfo.getCollegeName());
        if (collegeInfoService.count(wrapper) > 0) {
            return Result.error("该学院已存在，请勿重复添加");
        }
        collegeInfo.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        collegeInfoService.save(collegeInfo);
        return Result.success("新增学院成功");
    }

    /**
     * 修改学院信息（包含同步更新专业表的逻辑）
     */
    @PutMapping
    public Result<?> update(@RequestBody CollegeInfo collegeInfo) {
        if (collegeInfo.getId() == null) {
            return Result.error("学院ID不能为空");
        }

        // 1. 先查出修改前的旧学院信息
        CollegeInfo oldCollege = collegeInfoService.getById(collegeInfo.getId());
        if (oldCollege == null) {
            return Result.error("该学院不存在");
        }

        String oldName = oldCollege.getCollegeName();
        String newName = collegeInfo.getCollegeName();

        // 2. 判断：如果前端传来了新名字，且新名字和旧名字不一样，触发联动同步！
        if (org.springframework.util.StringUtils.hasText(newName) && !newName.equals(oldName)) {
            // 同步更新 Major 专业表中的 college_name 字段
            com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<com.zjy.xgxt.entity.Major> majorUpdate =
                    new com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper<>();

            majorUpdate.eq(com.zjy.xgxt.entity.Major::getCollegeName, oldName)
                    .set(com.zjy.xgxt.entity.Major::getCollegeName, newName);

            majorService.update(majorUpdate);
        }

        // 3. 最后更新学院表自身的其他信息（如院长名字等）
        collegeInfoService.updateById(collegeInfo);

        return Result.success("修改学院成功，关联的专业数据已自动同步！");
    }

    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        collegeInfoService.removeById(id);
        return Result.success("删除学院成功");
    }
}