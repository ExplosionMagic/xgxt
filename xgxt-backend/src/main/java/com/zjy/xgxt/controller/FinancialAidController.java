package com.zjy.xgxt.controller;

import com.alibaba.excel.EasyExcel;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.FinancialAid;
import com.zjy.xgxt.service.FinancialAidService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/api/aid")
@CrossOrigin
public class FinancialAidController {

    @Autowired
    private FinancialAidService financialAidService;

    /**
     * 1. 查询资助申请列表 (支持多角色权限过滤 & 关键字搜索)
     */
    @GetMapping("/list")
    public Result<List<FinancialAid>> list(
            @RequestParam String role,
            @RequestParam(required = false) String studentNo,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String keyword) { // 【新增】关键字搜索

        LambdaQueryWrapper<FinancialAid> wrapper = new LambdaQueryWrapper<>();

        if ("STUDENT".equals(role)) {
            // 学生只能看自己的
            wrapper.eq(FinancialAid::getStudentNo, studentNo);
        } else {
            // 教师和管理员的通用过滤逻辑
            if (status != null) {
                wrapper.eq(FinancialAid::getStatus, status);
            }
            // 【新增】支持按姓名或学号模糊搜索
            if (org.springframework.util.StringUtils.hasText(keyword)) {
                wrapper.and(w -> w.like(FinancialAid::getStudentName, keyword)
                        .or()
                        .like(FinancialAid::getStudentNo, keyword));
            }
        }

        wrapper.orderByDesc(FinancialAid::getId);
        return Result.success(financialAidService.list(wrapper));
    }
    /**
     * 2. 学生提交申请
     */
    @PostMapping("/apply")
    public Result<?> apply(@RequestBody FinancialAid aid) {
        // 校验是否已经有在途的申请（避免重复提交）
        LambdaQueryWrapper<FinancialAid> checkWrapper = new LambdaQueryWrapper<>();
        checkWrapper.eq(FinancialAid::getStudentNo, aid.getStudentNo())
                .in(FinancialAid::getStatus, 0, 2, 4);
        if (financialAidService.count(checkWrapper) > 0) {
            return Result.error("您已有正在审批或已通过的资助申请，无法重复提交");
        }

        aid.setStatus(0); // 初始状态：待教师审核
        aid.setApplyTime(new SimpleDateFormat("yyyy-MM-dd HH:mm").format(new Date()));
        financialAidService.save(aid);
        return Result.success("资助申请提交成功，请等待教师初审");
    }

    /**
     * 3. 教师初审
     */
    @PutMapping("/audit/teacher")
    public Result<?> teacherAudit(@RequestBody FinancialAid aid) {
        if (aid.getId() == null || aid.getStatus() == null) return Result.error("参数异常");
        // 状态只允许变更为 1(驳回) 或 2(通过，交由管理员)
        financialAidService.updateById(aid);
        return Result.success(aid.getStatus() == 2 ? "初审通过，已流转至管理员" : "已驳回该申请");
    }

    /**
     * 4. 管理员终审
     */
    @PutMapping("/audit/admin")
    public Result<?> adminAudit(@RequestBody FinancialAid aid) {
        if (aid.getId() == null || aid.getStatus() == null) return Result.error("参数异常");
        // 状态只允许变更为 3(驳回) 或 4(彻底通过)
        financialAidService.updateById(aid);
        return Result.success(aid.getStatus() == 4 ? "终审通过，已将该生列入资助名单" : "已驳回该申请");
    }

    /**
     * 学生撤销申请 (仅限待初审状态)
     */
    @DeleteMapping("/{id}")
    public Result<?> delete(@PathVariable Integer id) {
        financialAidService.removeById(id);
        return Result.success("申请已撤销");
    }

    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        // 1. 设置响应头信息，告诉浏览器这是一个 Excel 文件
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        // 设置导出的文件名
        String fileName = URLEncoder.encode("学生资助申请报表", "UTF-8").replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // 2. 查询全量数据 (如果你想按条件导出，可以在这里构造 LambdaQueryWrapper)
        List<FinancialAid> list = financialAidService.list();

        // 3. 将数据写入 Excel 流并输出
        EasyExcel.write(response.getOutputStream(), FinancialAid.class)
                .sheet("资助档案数据")
                .doWrite(list);
    }
}