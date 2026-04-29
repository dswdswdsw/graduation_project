package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.entity.*;
import com.campus.mapper.*;
import com.campus.service.DashboardService;
import com.campus.service.RedisService;
import com.campus.vo.DashboardVO;
import com.campus.vo.TodayCourseVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements DashboardService {

    private final CourseMapper courseMapper;
    private final CourseSelectionMapper courseSelectionMapper;
    private final HomeworkMapper homeworkMapper;
    private final HomeworkSubmissionMapper homeworkSubmissionMapper;
    private final UserMapper userMapper;
    private final RepairMapper repairMapper;
    private final NoticeMapper noticeMapper;
    private final NoticeReadMapper noticeReadMapper;
    private final ScoreMapper scoreMapper;
    private final ClassroomMapper classroomMapper;

    @Autowired(required = false)
    private RedisService redisService;

    @Override
    public DashboardVO getStudentDashboard(Long userId) {
        String cacheKey = "dashboard:student:" + userId;
        if (redisService != null) {
            DashboardVO cached = redisService.get(cacheKey, DashboardVO.class);
            if (cached != null) {
                log.debug("命中Redis缓存: {}", cacheKey);
                return cached;
            }
        }
        DashboardVO vo = buildStudentDashboard(userId);
        if (redisService != null) {
            redisService.set(cacheKey, vo, 2, TimeUnit.MINUTES);
            log.debug("写入Redis缓存: {}", cacheKey);
        }
        return vo;
    }

    @Override
    public DashboardVO getTeacherDashboard(Long userId) {
        String cacheKey = "dashboard:teacher:" + userId;
        if (redisService != null) {
            DashboardVO cached = redisService.get(cacheKey, DashboardVO.class);
            if (cached != null) {
                log.debug("命中Redis缓存: {}", cacheKey);
                return cached;
            }
        }
        DashboardVO vo = buildTeacherDashboard(userId);
        if (redisService != null) {
            redisService.set(cacheKey, vo, 2, TimeUnit.MINUTES);
            log.debug("写入Redis缓存: {}", cacheKey);
        }
        return vo;
    }

    @Override
    public DashboardVO getAdminDashboard() {
        String cacheKey = "dashboard:admin";
        if (redisService != null) {
            DashboardVO cached = redisService.get(cacheKey, DashboardVO.class);
            if (cached != null) {
                log.debug("命中Redis缓存: {}", cacheKey);
                return cached;
            }
        }
        DashboardVO vo = buildAdminDashboard();
        if (redisService != null) {
            redisService.set(cacheKey, vo, 5, TimeUnit.MINUTES);
            log.debug("写入Redis缓存: {}", cacheKey);
        }
        return vo;
    }

    private DashboardVO buildStudentDashboard(Long userId) {
        DashboardVO vo = new DashboardVO();

        LambdaQueryWrapper<CourseSelection> csWrapper = new LambdaQueryWrapper<>();
        csWrapper.eq(CourseSelection::getStudentId, userId).eq(CourseSelection::getStatus, 1);
        List<CourseSelection> selections = courseSelectionMapper.selectList(csWrapper);
        List<Long> courseIds = selections.stream().map(CourseSelection::getCourseId).collect(Collectors.toList());

        vo.setCourseCount(courseIds.size());

        if (!courseIds.isEmpty()) {
            LambdaQueryWrapper<Homework> hwWrapper = new LambdaQueryWrapper<>();
            hwWrapper.in(Homework::getCourseId, courseIds).eq(Homework::getStatus, 1);
            List<Homework> allHomework = homeworkMapper.selectList(hwWrapper);

            LambdaQueryWrapper<HomeworkSubmission> subWrapper = new LambdaQueryWrapper<>();
            subWrapper.eq(HomeworkSubmission::getStudentId, userId);
            Map<Long, HomeworkSubmission> submittedMap = homeworkSubmissionMapper.selectList(subWrapper).stream()
                    .collect(Collectors.toMap(HomeworkSubmission::getHomeworkId, Function.identity(), (a, b) -> a));

            long pending = allHomework.stream().filter(h -> {
                HomeworkSubmission sub = submittedMap.get(h.getId());
                if (sub == null) return true;
                return sub.getStatus() != null && sub.getStatus() == 2;
            }).count();
            vo.setHomeworkCount((int) pending);
        } else {
            vo.setHomeworkCount(0);
        }

        LambdaQueryWrapper<Repair> repairWrapper = new LambdaQueryWrapper<>();
        repairWrapper.eq(Repair::getUserId, userId).ne(Repair::getStatus, 2);
        vo.setRepairCount(Math.toIntExact(repairMapper.selectCount(repairWrapper)));

        LambdaQueryWrapper<NoticeRead> readWrapper = new LambdaQueryWrapper<>();
        readWrapper.eq(NoticeRead::getUserId, userId);
        long readCount = noticeReadMapper.selectCount(readWrapper);
        long totalNotices = noticeMapper.selectCount(new LambdaQueryWrapper<Notice>().eq(Notice::getStatus, 1));
        vo.setUnreadCount((int) Math.max(0L, totalNotices - readCount));

        return vo;
    }

    private DashboardVO buildTeacherDashboard(Long userId) {
        DashboardVO vo = new DashboardVO();

        LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<>();
        courseWrapper.eq(Course::getTeacherId, userId).eq(Course::getStatus, 1);
        List<Course> courses = courseMapper.selectList(courseWrapper);
        vo.setCourseCount(courses.size());

        int totalStudents = 0;
        for (Course c : courses) {
            totalStudents += c.getCurrentCount() != null ? c.getCurrentCount() : 0;
        }
        vo.setStudentCount(totalStudents);

        List<Long> courseIds = courses.stream().map(Course::getId).collect(Collectors.toList());
        if (!courseIds.isEmpty()) {
            LambdaQueryWrapper<Homework> hwWrapper = new LambdaQueryWrapper<>();
            hwWrapper.in(Homework::getCourseId, courseIds).eq(Homework::getStatus, 1);
            List<Homework> homeworks = homeworkMapper.selectList(hwWrapper);

            long pendingGrade = 0;
            for (Homework hw : homeworks) {
                LambdaQueryWrapper<HomeworkSubmission> subWrapper = new LambdaQueryWrapper<>();
                subWrapper.eq(HomeworkSubmission::getHomeworkId, hw.getId())
                        .eq(HomeworkSubmission::getStatus, 0);
                pendingGrade += homeworkSubmissionMapper.selectCount(subWrapper);
            }
            vo.setPendingScore((int) pendingGrade);
        } else {
            vo.setPendingScore(0);
        }

        LambdaQueryWrapper<NoticeRead> readWrapper = new LambdaQueryWrapper<>();
        readWrapper.eq(NoticeRead::getUserId, userId);
        long readCount = noticeReadMapper.selectCount(readWrapper);
        long totalNotices = noticeMapper.selectCount(new LambdaQueryWrapper<Notice>().eq(Notice::getStatus, 1));
        vo.setUnreadCount((int) Math.max(0L, totalNotices - readCount));

        return vo;
    }

    private DashboardVO buildAdminDashboard() {
        DashboardVO vo = new DashboardVO();

        vo.setUserCount(Math.toIntExact(userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getStatus, 1))));
        vo.setStudentCount(Math.toIntExact(userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRole, "student").eq(User::getStatus, 1))));
        vo.setTeacherCount(Math.toIntExact(userMapper.selectCount(
                new LambdaQueryWrapper<User>().eq(User::getRole, "teacher").eq(User::getStatus, 1))));

        vo.setCourseCount(Math.toIntExact(courseMapper.selectCount(
                new LambdaQueryWrapper<Course>().eq(Course::getStatus, 1))));

        vo.setSelectionCount(Math.toIntExact(courseSelectionMapper.selectCount(
                new LambdaQueryWrapper<CourseSelection>().eq(CourseSelection::getStatus, 1))));

        vo.setClassroomCount(Math.toIntExact(classroomMapper.selectCount(
                new LambdaQueryWrapper<Classroom>().eq(Classroom::getStatus, 1))));

        vo.setPendingRepairCount(Math.toIntExact(repairMapper.selectCount(
                new LambdaQueryWrapper<Repair>().eq(Repair::getStatus, 0))));
        vo.setTotalRepairCount(Math.toIntExact(repairMapper.selectCount(new LambdaQueryWrapper<>())));

        vo.setNoticeCount(Math.toIntExact(noticeMapper.selectCount(
                new LambdaQueryWrapper<Notice>().eq(Notice::getStatus, 1))));

        return vo;
    }

    @Override
    public List<TodayCourseVO> getTodayCourses(Long userId, String role) {
        String cacheKey = "dashboard:today:" + role + ":" + userId;
        if (redisService != null) {
            @SuppressWarnings("unchecked")
            List<TodayCourseVO> cached = redisService.get(cacheKey, List.class);
            if (cached != null) {
                log.debug("命中Redis缓存: {}", cacheKey);
                return cached;
            }
        }

        List<TodayCourseVO> result = buildTodayCourses(userId, role);

        if (redisService != null) {
            redisService.set(cacheKey, result, 10, TimeUnit.MINUTES);
            log.debug("写入Redis缓存: {}", cacheKey);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    private List<TodayCourseVO> buildTodayCourses(Long userId, String role) {
        int weekday = LocalDateTime.now().getDayOfWeek().getValue();

        List<Course> courses;
        if ("student".equals(role)) {
            LambdaQueryWrapper<CourseSelection> csWrapper = new LambdaQueryWrapper<>();
            csWrapper.eq(CourseSelection::getStudentId, userId).eq(CourseSelection::getStatus, 1);
            List<Long> courseIds = courseSelectionMapper.selectList(csWrapper).stream()
                    .map(CourseSelection::getCourseId).collect(Collectors.toList());
            if (courseIds.isEmpty()) return new ArrayList<>();
            courses = courseMapper.selectList(new LambdaQueryWrapper<Course>()
                    .in(Course::getId, courseIds).eq(Course::getWeekday, weekday).eq(Course::getStatus, 1));
        } else if ("teacher".equals(role)) {
            courses = courseMapper.selectList(new LambdaQueryWrapper<Course>()
                    .eq(Course::getTeacherId, userId).eq(Course::getWeekday, weekday).eq(Course::getStatus, 1));
        } else {
            courses = courseMapper.selectList(new LambdaQueryWrapper<Course>()
                    .eq(Course::getWeekday, weekday).eq(Course::getStatus, 1));
        }

        List<Long> teacherIds = courses.stream().map(Course::getTeacherId).distinct().collect(Collectors.toList());
        Map<Long, User> teacherMap = teacherIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(teacherIds).stream().collect(Collectors.toMap(User::getId, Function.identity()));

        return courses.stream().map(c -> {
            TodayCourseVO vo = new TodayCourseVO();
            vo.setCourseId(c.getId());
            vo.setCourseName(c.getName());
            vo.setCode(c.getCode());
            vo.setWeekday(c.getWeekday());
            vo.setStartSection(c.getStartSection());
            vo.setEndSection(c.getEndSection());
            vo.setLocation(c.getLocation());
            vo.setCurrentCount(c.getCurrentCount());
            vo.setMaxStudents(c.getMaxStudents());
            vo.setCourseType(c.getCourseType());
            User t = teacherMap.get(c.getTeacherId());
            vo.setTeacherName(t != null ? t.getRealName() : "");
            return vo;
        }).sorted((a, b) -> a.getStartSection() - b.getStartSection()).collect(Collectors.toList());
    }
}
