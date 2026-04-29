package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.entity.Course;
import com.campus.entity.CourseSelection;
import com.campus.entity.User;
import com.campus.exception.BusinessException;
import com.campus.mapper.CourseMapper;
import com.campus.mapper.CourseSelectionMapper;
import com.campus.mapper.UserMapper;
import com.campus.service.CourseSelectionService;
import com.campus.service.RedisService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import com.campus.vo.MyCourseVO;

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseSelectionServiceImpl extends ServiceImpl<CourseSelectionMapper, CourseSelection> implements CourseSelectionService {

    private final CourseMapper courseMapper;
    private final UserMapper userMapper;

    @Autowired(required = false)
    private RedisService redisService;

    private String stockKey(Long courseId) {
        return "course:stock:" + courseId;
    }

    private void initStockCache(Long courseId) {
        if (redisService == null) return;
        String key = stockKey(courseId);
        if (!Boolean.TRUE.equals(redisService.hasKey(key))) {
            Course course = courseMapper.selectById(courseId);
            if (course != null) {
                int remaining = course.getMaxStudents() - (course.getCurrentCount() != null ? course.getCurrentCount() : 0);
                redisService.setIfAbsent(key, remaining, 30, TimeUnit.MINUTES);
                log.debug("初始化课程库存缓存: courseId={}, remaining={}", courseId, remaining);
            }
        }
    }

    @Override
    @Transactional
    public void selectCourse(Long studentId, Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null || course.getStatus() != 1) {
            throw new BusinessException("课程不存在或已停用");
        }

        LambdaQueryWrapper<CourseSelection> existWrapper = new LambdaQueryWrapper<>();
        existWrapper.eq(CourseSelection::getStudentId, studentId)
                .eq(CourseSelection::getCourseId, courseId)
                .eq(CourseSelection::getStatus, 1);
        if (count(existWrapper) > 0) {
            throw new BusinessException("已选择该课程");
        }

        LambdaQueryWrapper<CourseSelection> conflictWrapper = new LambdaQueryWrapper<>();
        conflictWrapper.eq(CourseSelection::getStudentId, studentId)
                .eq(CourseSelection::getStatus, 1);
        List<CourseSelection> selections = list(conflictWrapper);
        for (CourseSelection sel : selections) {
            Course existing = courseMapper.selectById(sel.getCourseId());
            if (existing != null && existing.getWeekday().equals(course.getWeekday())) {
                boolean overlap = !(course.getEndSection() < existing.getStartSection()
                        || course.getStartSection() > existing.getEndSection());
                if (overlap) {
                    throw new BusinessException("与已选课程时间冲突：" + existing.getName());
                }
            }
        }

        if (redisService != null) {
            initStockCache(courseId);
        } else {
            if (course.getCurrentCount() >= course.getMaxStudents()) {
                throw new BusinessException("课程已满");
            }
        }

        int updated = courseMapper.update(null,
                new LambdaUpdateWrapper<Course>()
                        .eq(Course::getId, courseId)
                        .lt(Course::getCurrentCount, course.getMaxStudents())
                        .setSql("current_count = current_count + 1"));
        if (updated == 0) {
            throw new BusinessException("课程已满");
        }

        if (redisService != null) {
            String key = stockKey(courseId);
            redisService.decrement(key);
            log.debug("Redis库存扣减: courseId={}", courseId);
        }

        CourseSelection selection = new CourseSelection();
        selection.setStudentId(studentId);
        selection.setCourseId(courseId);
        selection.setStatus(1);
        save(selection);

        log.info("学生{}成功选择课程{}", studentId, courseId);
    }

    @Override
    @Transactional
    public void dropCourse(Long studentId, Long courseId) {
        LambdaQueryWrapper<CourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseSelection::getStudentId, studentId)
                .eq(CourseSelection::getCourseId, courseId)
                .eq(CourseSelection::getStatus, 1);
        CourseSelection selection = getOne(wrapper);
        if (selection == null) {
            throw new BusinessException("未选择该课程");
        }
        selection.setStatus(0);
        updateById(selection);

        Course course = courseMapper.selectById(courseId);
        if (course != null && course.getCurrentCount() > 0) {
            courseMapper.update(null,
                    new LambdaUpdateWrapper<Course>()
                            .eq(Course::getId, courseId)
                            .setSql("current_count = current_count - 1"));
        }

        if (redisService != null) {
            String key = stockKey(courseId);
            if (Boolean.TRUE.equals(redisService.hasKey(key))) {
                redisService.increment(key);
                log.debug("Redis库存回补: courseId={}", courseId);
            }
        }

        log.info("学生{}退选课程{}", studentId, courseId);
    }

    @Override
    public Page<MyCourseVO> listMySelections(Long studentId, String semester, int page, int size) {
        LambdaQueryWrapper<Course> electiveWrapper = new LambdaQueryWrapper<>();
        electiveWrapper.eq(Course::getCourseType, "选修").eq(Course::getStatus, 1)
                .select(Course::getId);
        List<Long> electiveIds = courseMapper.selectList(electiveWrapper).stream()
                .map(Course::getId).collect(Collectors.toList());
        if (electiveIds.isEmpty()) {
            Page<MyCourseVO> empty = new Page<>(page, size, 0);
            empty.setRecords(new java.util.ArrayList<>());
            return empty;
        }
        Page<CourseSelection> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<CourseSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(CourseSelection::getStudentId, studentId)
                .eq(CourseSelection::getStatus, 1)
                .in(CourseSelection::getCourseId, electiveIds)
                .orderByDesc(CourseSelection::getCreateTime);
        Page<CourseSelection> selPage = page(pageParam, wrapper);
        if (selPage.getRecords().isEmpty()) {
            Page<MyCourseVO> empty = new Page<>(page, size, 0);
            empty.setRecords(new java.util.ArrayList<>());
            return empty;
        }
        List<Long> courseIds = selPage.getRecords().stream().map(CourseSelection::getCourseId).distinct().collect(Collectors.toList());
        Map<Long, Course> courseMap = courseMapper.selectBatchIds(courseIds).stream()
                .collect(Collectors.toMap(Course::getId, c -> c));
        List<Long> teacherIds = courseMap.values().stream().map(Course::getTeacherId).distinct().collect(Collectors.toList());
        Map<Long, User> teacherMap = teacherIds.isEmpty() ? java.util.Map.of() :
                userMapper.selectBatchIds(teacherIds).stream()
                        .collect(Collectors.toMap(User::getId, u -> u));
        Page<MyCourseVO> voPage = new Page<>(selPage.getCurrent(), selPage.getSize(), selPage.getTotal());
        voPage.setRecords(selPage.getRecords().stream().map(sel -> {
            MyCourseVO vo = new MyCourseVO();
            vo.setSelectionId(sel.getId());
            vo.setCourseId(sel.getCourseId());
            vo.setCreateTime(sel.getCreateTime());
            Course c = courseMap.get(sel.getCourseId());
            vo.setCode(c.getCode());
            vo.setName(c.getName());
            vo.setCredit(c.getCredit());
            vo.setHours(c.getHours());
            vo.setSemester(c.getSemester());
            vo.setWeekday(c.getWeekday());
            vo.setStartSection(c.getStartSection());
            vo.setEndSection(c.getEndSection());
            vo.setLocation(c.getLocation());
            vo.setCourseType(c.getCourseType());
            User t = teacherMap.get(c.getTeacherId());
            if (t != null) { vo.setTeacherName(t.getRealName()); }
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    @Transactional
    public int batchAssignToClass(Long courseId, String className) {
        Course course = courseMapper.selectById(courseId);
        if (course == null || course.getStatus() != 1) {
            throw new BusinessException("课程不存在或已停用");
        }

        LambdaQueryWrapper<User> studentWrapper = new LambdaQueryWrapper<>();
        studentWrapper.eq(User::getRole, "student")
                .eq(User::getClassName, className)
                .eq(User::getStatus, 1)
                .select(User::getId);
        List<User> students = userMapper.selectList(studentWrapper);

        if (students.isEmpty()) {
            throw new BusinessException("该班级没有学生：" + className);
        }

        int count = 0;
        for (User student : students) {
            LambdaQueryWrapper<CourseSelection> existWrapper = new LambdaQueryWrapper<>();
            existWrapper.eq(CourseSelection::getStudentId, student.getId())
                    .eq(CourseSelection::getCourseId, courseId)
                    .eq(CourseSelection::getStatus, 1);
            if (count(existWrapper) > 0) {
                continue;
            }
            CourseSelection selection = new CourseSelection();
            selection.setStudentId(student.getId());
            selection.setCourseId(courseId);
            selection.setStatus(1);
            save(selection);
            count++;
        }

        courseMapper.update(null,
                new LambdaUpdateWrapper<Course>()
                        .eq(Course::getId, courseId)
                        .setSql("current_count = current_count + " + count));

        log.info("管理员为班级[{}]排课[{}], 新增选课记录{}条", className, course.getName(), count);
        return count;
    }

    @Override
    public List<String> listAllClasses() {
        return userMapper.selectList(
                new LambdaQueryWrapper<User>()
                        .eq(User::getRole, "student")
                        .isNotNull(User::getClassName)
                        .ne(User::getClassName, "")
                        .select(User::getClassName)
        ).stream().map(User::getClassName).distinct().sorted().collect(Collectors.toList());
    }
}
