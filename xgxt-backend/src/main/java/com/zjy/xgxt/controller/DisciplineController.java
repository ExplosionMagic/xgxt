package com.zjy.xgxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.DisciplinaryRecord;
import com.zjy.xgxt.service.DisciplinaryRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/discipline")
@CrossOrigin
public class DisciplineController {

    @Autowired
    private DisciplinaryRecordService disciplineService;

    /**
     * 获取违纪记录列表 (严格按角色隔离权限)
     */
    @GetMapping("/list")
    public Result<List<DisciplinaryRecord>> list(
            @RequestParam String role,
            @RequestParam(required = false) String userNo,
            @RequestParam(required = false) String userName,
            @RequestParam(required = false) String keyword) {

        LambdaQueryWrapper<DisciplinaryRecord> wrapper = new LambdaQueryWrapper<>();

        if ("STUDENT".equals(role)) {
            // 学生只能看到自己的记录（无论是否生效，都可以看到进度）
            wrapper.eq(DisciplinaryRecord::getStudentNo, userNo);
        } else if ("TEACHER".equals(role)) {
            // 教师只能看到自己上报的记录
            wrapper.eq(DisciplinaryRecord::getReporter, userName);
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(DisciplinaryRecord::getStudentName, keyword)
                        .or().like(DisciplinaryRecord::getStudentNo, keyword));
            }
        } else if ("ADMIN".equals(role)) {
            // 管理员看全校的，支持搜索
            if (StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(DisciplinaryRecord::getStudentName, keyword)
                        .or().like(DisciplinaryRecord::getStudentNo, keyword));
            }
        }

        wrapper.orderByDesc(DisciplinaryRecord::getId);
        return Result.success(disciplineService.list(wrapper));
    }

    /**
     * 教师发起违纪上报
     */
    @PostMapping("/report")
    public Result<?> report(@RequestBody DisciplinaryRecord record) {
        record.setStatus(0); // 默认待管理员审批
        record.setCreateTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        disciplineService.save(record);
        return Result.success("违纪上报成功，已提交管理员审核");
    }

    /**
     * 管理员审批 (通过或驳回)
     */
    @PutMapping("/audit")
    public Result<?> audit(@RequestBody DisciplinaryRecord record) {
        if (record.getId() == null || record.getStatus() == null) {
            return Result.error("参数异常");
        }

        // 先查出修改前的状态
        DisciplinaryRecord oldRecord = disciplineService.getById(record.getId());

        // 更新到新状态
        disciplineService.updateById(record);

        // 根据不同场景返回精准的提示语
        if (record.getStatus() == 1) {
            return Result.success("审批通过，处分已生效");
        } else if (record.getStatus() == 2 && oldRecord.getStatus() == 1) {
            return Result.success("该处分已成功撤销"); // 已生效 -> 撤销
        } else {
            return Result.success("已驳回该上报"); // 待审批 -> 驳回
        }
    }

    /**
     * 删除/撤销上报记录
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        disciplineService.removeById(id);
        return Result.success("记录已删除");
    }
}