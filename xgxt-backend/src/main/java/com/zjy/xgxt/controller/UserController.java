package com.zjy.xgxt.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.zjy.xgxt.common.Result;
import com.zjy.xgxt.entity.Course;
import com.zjy.xgxt.entity.User;
import com.zjy.xgxt.service.CourseService;
import com.zjy.xgxt.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.util.DigestUtils;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.multipart.MultipartFile;
import com.alibaba.excel.EasyExcel;

import java.util.Map;
import java.util.List;
import java.util.Collections;

@RestController
@RequestMapping("/api/user")
@CrossOrigin
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private CourseService courseService; // 【新增】注入课程服务，用于修改教师名时同步

    /**
     * 1. 根据学号/职工ID获取个人详细信息 (用于个人中心)
     */
    @GetMapping("/info/{userNo}")
    public Result<User> getInfo(@PathVariable String userNo) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUserNo, userNo);
        User user = userService.getOne(wrapper);
        if (user != null) {
            user.setPassword(null); // 返回给前端前抹除密码，保证安全
        }
        return Result.success(user);
    }

    /**
     * 2. 个人自己修改信息 (严格限制：只能修改手机号、联系地址、密码)
     */
    @PutMapping("/profile")
    public Result<?> updateProfile(@RequestBody User user) {
        if (user.getId() == null) {
            return Result.error("用户ID不能为空");
        }

        // 先从数据库查出原信息
        User dbUser = userService.getById(user.getId());
        if (dbUser == null) {
            return Result.error("用户不存在");
        }

        // 如果修改了手机号，需校验唯一性
        if (StringUtils.hasText(user.getPhone()) && !user.getPhone().equals(dbUser.getPhone())) {
            LambdaQueryWrapper<User> phoneQuery = new LambdaQueryWrapper<>();
            phoneQuery.eq(User::getPhone, user.getPhone());
            if (userService.count(phoneQuery) > 0) {
                return Result.error("该手机号已被其他账号绑定");
            }
            dbUser.setPhone(user.getPhone());
        }

        // 只允许覆盖这三个字段，防止前端恶意传参修改角色或姓名
        if (StringUtils.hasText(user.getAddress())) {
            dbUser.setAddress(user.getAddress());
        }
        if (StringUtils.hasText(user.getPassword())) {
            dbUser.setPassword(user.getPassword());
        }

        userService.updateById(dbUser);
        return Result.success("个人信息保存成功");
    }

    /**
     * 3. 管理员获取所有用户列表 (带条件模糊搜索)
     */
    @GetMapping("/list")
    public Result<List<User>> list(@RequestParam(required = false) String name) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(name)) {
            // 支持按姓名或学号模糊查询
            wrapper.like(User::getName, name).or().like(User::getUserNo, name);
        }
        wrapper.orderByDesc(User::getId);

        List<User> list = userService.list(wrapper);
        // 批量抹除密码返回
        list.forEach(u -> u.setPassword(null));

        return Result.success(list);
    }

    /**
     * 4. 管理员高级更新 (除用户组和学号外，可修改任意信息)
     */
    @PutMapping("/admin-update")
    public Result<?> adminUpdate(@RequestBody User user) {
        if (user.getId() == null) {
            return Result.error("用户ID不能为空");
        }

        // 必须先从数据库查出该用户原来的信息
        User dbUser = userService.getById(user.getId());
        if (dbUser == null) {
            return Result.error("用户不存在");
        }

        // 如果管理员修改了该用户的手机号，需要校验唯一性
        if (StringUtils.hasText(user.getPhone()) && !user.getPhone().equals(dbUser.getPhone())) {
            LambdaQueryWrapper<User> phoneQuery = new LambdaQueryWrapper<>();
            phoneQuery.eq(User::getPhone, user.getPhone());
            if (userService.count(phoneQuery) > 0) {
                return Result.error("该手机号已被使用，请更换");
            }
        }

        // 判断：如果该用户是老师，并且管理员修改了老师的名字
        if ("TEACHER".equals(dbUser.getRole()) && StringUtils.hasText(user.getName()) && !user.getName().equals(dbUser.getName())) {
            // 同步更新课程表里对应的任课教师名称
            LambdaUpdateWrapper<Course> courseUpdate = new LambdaUpdateWrapper<>();
            courseUpdate.eq(Course::getTeacherName, dbUser.getName()) // 找旧名字
                        .set(Course::getTeacherName, user.getName()); // 换新名字
            courseService.update(courseUpdate);
        }

        user.setRole(dbUser.getRole());
        user.setUserNo(dbUser.getUserNo());

        // 如果密码框是空的，则保留原密码不修改
        if (!StringUtils.hasText(user.getPassword())) {
            user.setPassword(dbUser.getPassword());
        } else {
            // 如果前端填了新密码，也用MD5加密
            user.setPassword(DigestUtils.md5DigestAsHex(user.getPassword().getBytes()));
        }

        // 更新数据库
        userService.updateById(user);
        return Result.success("用户信息更新成功");
    }

    /**
     * 5. 修改密码
     */
    @PutMapping("/password")
    public Result<?> updatePassword(@RequestBody Map<String, String> params) {
        Integer id = Integer.parseInt(params.get("id"));
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");

        User dbUser = userService.getById(id);
        if (dbUser == null) {
            return Result.error("用户不存在");
        }

        // 1. 验证旧密码是否正确 (将前端传来的明文旧密码进行MD5加密，再与数据库比对)
        String md5OldPassword = DigestUtils.md5DigestAsHex(oldPassword.getBytes());

        // 兼容一下测试期的明文密码：如果数据库密码既不等于密文，也不等于明文，就报错
        if (!dbUser.getPassword().equals(md5OldPassword) && !dbUser.getPassword().equals(oldPassword)) {
            return Result.error("原密码错误");
        }

        // 2. 验证新旧密码是否相同
        if (oldPassword.equals(newPassword)) {
            return Result.error("新密码不能与原密码相同");
        }

        // 3. 将新密码MD5加密后存入数据库
        String md5NewPassword = DigestUtils.md5DigestAsHex(newPassword.getBytes());
        dbUser.setPassword(md5NewPassword);
        userService.updateById(dbUser);

        return Result.success("密码修改成功");
    }
        // 用户信息报表导入
        @PostMapping("/import")
        public Result<?> importUsers(MultipartFile file) {
            try {
                // 1. 使用EasyExcel同步读取上传的文件流
                List<User> userList = EasyExcel.read(file.getInputStream())
                        .head(User.class) // 以 User 类的 @ExcelProperty 注解作为表头映射
                        .sheet()
                        .doReadSync();

                // 2. 遍历检查并入库
                int successCount = 0;
                for (User user : userList) {
                    // 如果账号为空，跳过这一行
                    if (user.getUserNo() == null || user.getUserNo().isEmpty()) {
                        continue;
                    }

                    // 检查数据库中是否已经存在该学号/工号，防止重复插入报错
                    long count = userService.count(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<User>()
                            .eq(User::getUserNo, user.getUserNo()));

                    if (count == 0) {
                        // 如果 Excel 里没填密码，给个默认密码 123456
                        if (user.getPassword() == null || user.getPassword().isEmpty()) {
                            user.setPassword("123456");
                        }
                        // 默认赋予学生角色（如果 Excel 里没填）
                        if (user.getRole() == null || user.getRole().isEmpty()) {
                            user.setRole("STUDENT");
                        }
                        // 默认启用账号
                        user.setStatus(1);

                        userService.save(user);
                        successCount++;
                    }
                }
                return Result.success("成功导入" + successCount + "条新用户数据！");

            } catch (Exception e) {
                e.printStackTrace();
                return Result.error("导入失败，请检查格式是否正确，或报表内数据有误");
            }
        }

        // 用户信息报表导出
        @GetMapping("/export")
        public void export(HttpServletResponse response) throws Exception {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String fileName = java.net.URLEncoder.encode("系统用户报表", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");
            // 写入并导出
            com.alibaba.excel.EasyExcel.write(response.getOutputStream(), User.class)
                    .sheet("用户名单").doWrite(userService.list());
        }

        @GetMapping("/template")
        public void downloadTemplate(HttpServletResponse response) throws Exception {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 设置模板的文件名
            String fileName = java.net.URLEncoder.encode("用户信息导入模板", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            // 传入Collections.emptyList()，导出的报表只有表头，不包含数据
            com.alibaba.excel.EasyExcel.write(response.getOutputStream(), User.class)
                    .sheet("导入模板")
                    .doWrite(Collections.emptyList());
        }
}