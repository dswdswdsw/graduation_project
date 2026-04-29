package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dto.CourseDTO;
import com.campus.entity.Classroom;
import com.campus.entity.Course;
import com.campus.entity.CourseSelection;
import com.campus.entity.User;
import com.campus.exception.BusinessException;
import com.campus.mapper.ClassroomMapper;
import com.campus.mapper.CourseMapper;
import com.campus.mapper.CourseSelectionMapper;
import com.campus.mapper.UserMapper;
import com.campus.service.CourseService;
import com.campus.vo.AdminScheduleVO;
import com.campus.vo.ClassroomVO;
import com.campus.vo.CourseVO;
import com.campus.vo.ScheduleVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private final UserMapper userMapper;
    private final CourseSelectionMapper courseSelectionMapper;
    private final ClassroomMapper classroomMapper;

    @Override
    public Page<CourseVO> listCourses(String semester, String keyword, int page, int size) {
        Page<Course> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(semester)) { wrapper.eq(Course::getSemester, semester); }
        if (StringUtils.hasText(keyword)) { wrapper.and(w -> w.like(Course::getName, keyword).or().like(Course::getCode, keyword)); }
        wrapper.eq(Course::getStatus, 1)
              .eq(Course::getCourseType, "选修")
              .orderByAsc(Course::getWeekday, Course::getStartSection);
        Page<Course> result = page(pageParam, wrapper);
        Map<String, String> roomDisplayMap = buildRoomDisplayMap(result.getRecords());
        List<Long> teacherIds = result.getRecords().stream().map(Course::getTeacherId).distinct().collect(Collectors.toList());
        Map<Long, User> teacherMap = Map.of();
        if (!teacherIds.isEmpty()) { teacherMap = userMapper.selectBatchIds(teacherIds).stream().collect(Collectors.toMap(User::getId, Function.identity())); }
        Map<Long, User> finalTeacherMap = teacherMap;
        Page<CourseVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(c -> {
            CourseVO vo = new CourseVO();
            BeanUtils.copyProperties(c, vo);
            User t = finalTeacherMap.get(c.getTeacherId());
            if (t != null) { vo.setTeacherName(t.getRealName()); }
            vo.setRemainCount(c.getMaxStudents() - c.getCurrentCount());
            vo.setLocationDisplay(roomDisplayMap.getOrDefault(c.getLocation(), c.getLocation()));
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public CourseVO getCourseDetail(Long id) {
        Course course = getById(id);
        if (course == null) { throw new BusinessException("课程不存在"); }
        CourseVO vo = new CourseVO();
        BeanUtils.copyProperties(course, vo);
        User t = userMapper.selectById(course.getTeacherId());
        if (t != null) { vo.setTeacherName(t.getRealName()); }
        vo.setRemainCount(course.getMaxStudents() - course.getCurrentCount());
        return vo;
    }

    @Override
    public void addCourse(CourseDTO dto) {
        Long count = lambdaQuery().eq(Course::getCode, dto.getCode()).count();
        if (count > 0) { throw new BusinessException("课程编号已存在"); }

        String conflict = checkCourseConflict(dto);
        if (conflict != null) { throw new BusinessException(conflict); }

        Course course = new Course();
        BeanUtils.copyProperties(dto, course);
        course.setCurrentCount(0); course.setVersion(0); course.setStatus(1);
        save(course);
    }

    @Override
    public void updateCourse(CourseDTO dto) {
        Course course = getById(dto.getId());
        if (course == null) { throw new BusinessException("课程不存在"); }

        if (dto.getWeekday() != null || dto.getStartSection() != null || dto.getEndSection() != null
                || dto.getLocation() != null || dto.getTeacherId() != null || dto.getSemester() != null) {
            CourseDTO checkDto = new CourseDTO();
            checkDto.setId(dto.getId());
            checkDto.setWeekday(dto.getWeekday() != null ? dto.getWeekday() : course.getWeekday());
            checkDto.setStartSection(dto.getStartSection() != null ? dto.getStartSection() : course.getStartSection());
            checkDto.setEndSection(dto.getEndSection() != null ? dto.getEndSection() : course.getEndSection());
            checkDto.setLocation(dto.getLocation() != null ? dto.getLocation() : course.getLocation());
            checkDto.setTeacherId(dto.getTeacherId() != null ? dto.getTeacherId() : course.getTeacherId());
            checkDto.setSemester(dto.getSemester() != null ? dto.getSemester() : course.getSemester());
            String conflict = checkCourseConflict(checkDto);
            if (conflict != null) { throw new BusinessException(conflict); }
        }

        if (dto.getCode() != null) course.setCode(dto.getCode());
        if (dto.getName() != null) course.setName(dto.getName());
        if (dto.getTeacherId() != null) course.setTeacherId(dto.getTeacherId());
        if (dto.getCredit() != null) course.setCredit(dto.getCredit());
        if (dto.getHours() != null) course.setHours(dto.getHours());
        if (dto.getSemester() != null) course.setSemester(dto.getSemester());
        if (dto.getWeekday() != null) course.setWeekday(dto.getWeekday());
        if (dto.getStartSection() != null) course.setStartSection(dto.getStartSection());
        if (dto.getEndSection() != null) course.setEndSection(dto.getEndSection());
        if (dto.getLocation() != null) course.setLocation(dto.getLocation());
        if (dto.getMaxStudents() != null) course.setMaxStudents(dto.getMaxStudents());
        if (dto.getDescription() != null) course.setDescription(dto.getDescription());
        updateById(course);
    }

    @Override
    public void deleteCourse(Long id) { removeById(id); }

    @Override
    public List<ScheduleVO> getStudentSchedule(Long studentId, String semester) {
        LambdaQueryWrapper<CourseSelection> sw = new LambdaQueryWrapper<>();
        sw.eq(CourseSelection::getStudentId, studentId).eq(CourseSelection::getStatus, 1);
        List<CourseSelection> selections = courseSelectionMapper.selectList(sw);
        if (selections.isEmpty()) { return new ArrayList<>(); }
        List<Long> cids = selections.stream().map(CourseSelection::getCourseId).collect(Collectors.toList());
        LambdaQueryWrapper<Course> cw = new LambdaQueryWrapper<>();
        cw.in(Course::getId, cids).eq(StringUtils.hasText(semester), Course::getSemester, semester).eq(Course::getStatus, 1);
        List<Course> courses = list(cw);
        Map<String, String> roomMap = buildRoomDisplayMap(courses);
        List<Long> tids = courses.stream().map(Course::getTeacherId).distinct().collect(Collectors.toList());
        Map<Long, User> tm = Map.of();
        if (!tids.isEmpty()) { tm = userMapper.selectBatchIds(tids).stream().collect(Collectors.toMap(User::getId, Function.identity())); }
        Map<Long, User> ftm = tm;
        return courses.stream().map(c -> {
            ScheduleVO vo = new ScheduleVO();
            vo.setCourseId(c.getId()); vo.setCourseName(c.getName()); vo.setCode(c.getCode());
            vo.setWeekday(c.getWeekday()); vo.setStartSection(c.getStartSection()); vo.setEndSection(c.getEndSection());
            vo.setLocation(c.getLocation());
            vo.setLocationDisplay(roomMap.getOrDefault(c.getLocation(), c.getLocation()));
            vo.setCredit(c.getCredit()); vo.setHours(c.getHours());
            vo.setCourseType(c.getCourseType());
            vo.setCurrentCount(c.getCurrentCount()); vo.setMaxStudents(c.getMaxStudents());
            User t2 = ftm.get(c.getTeacherId());
            vo.setTeacherName(t2 != null ? t2.getRealName() : "");
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public Page<AdminScheduleVO> listAdminSchedule(String semester, String className, Long teacherId, String courseType, String keyword, int page, int size) {
        LambdaQueryWrapper<Course> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(semester)) w.eq(Course::getSemester, semester);
        if (teacherId != null) w.eq(Course::getTeacherId, teacherId);
        if (StringUtils.hasText(courseType)) w.eq(Course::getCourseType, courseType);
        if (StringUtils.hasText(keyword)) { w.and(x -> x.like(Course::getName, keyword).or().like(Course::getCode, keyword)); }
        w.eq(Course::getStatus, 1).orderByAsc(Course::getWeekday, Course::getStartSection);

        List<Course> allCourses = list(w);

        if (allCourses.isEmpty()) { return new Page<>(page, size, 0); }

        List<Long> allCourseIds = allCourses.stream().map(Course::getId).collect(Collectors.toList());
        Map<Long, List<String>> courseClassMap = getCourseClassList(allCourseIds);
        Map<Long, Integer> sm = getCourseStudentCount(allCourseIds);
        Map<String, String> adminRoomMap = buildRoomDisplayMap(allCourses);

        List<Long> tids = allCourses.stream().map(Course::getTeacherId).distinct().collect(Collectors.toList());
        Map<Long, User> teacherMap = tids.isEmpty() ? Map.of() : userMapper.selectBatchIds(tids).stream().collect(Collectors.toMap(User::getId, Function.identity()));

        List<AdminScheduleVO> flatRecords = new ArrayList<>();
        for (Course c : allCourses) {
            List<String> classes = courseClassMap.getOrDefault(c.getId(), List.of());

            if (StringUtils.hasText(className) && !classes.contains(className)) {
                continue;
            }
            if (classes.isEmpty()) {
                classes = List.of("");
            }
            for (String cls : classes) {
                AdminScheduleVO vo = new AdminScheduleVO();
                BeanUtils.copyProperties(c, vo);
                User t = teacherMap.get(c.getTeacherId());
                if (t != null) { vo.setTeacherName(t.getRealName()); }
                vo.setRemainCount(c.getMaxStudents() != null ? c.getMaxStudents() - c.getCurrentCount() : null);
                vo.setClassName(cls);
                vo.setStudentCount(sm.getOrDefault(c.getId(), 0));
                vo.setLocationDisplay(adminRoomMap.getOrDefault(c.getLocation(), c.getLocation()));
                flatRecords.add(vo);
            }
        }

        int total = flatRecords.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        List<AdminScheduleVO> pageRecords = start < total ? flatRecords.subList(start, end) : new ArrayList<>();

        Page<AdminScheduleVO> vp = new Page<>(page, size, total);
        vp.setRecords(pageRecords);
        return vp;
    }

    private List<Long> findCourseIdsByClass(String className) {
        LambdaQueryWrapper<User> uw = new LambdaQueryWrapper<>();
        uw.eq(User::getRole, "student").eq(User::getClassName, className).eq(User::getStatus, 1).select(User::getId);
        List<User> stus = userMapper.selectList(uw);
        if (stus.isEmpty()) { return List.of(); }
        List<Long> sids = stus.stream().map(User::getId).collect(Collectors.toList());
        LambdaQueryWrapper<CourseSelection> cw = new LambdaQueryWrapper<>();
        cw.in(CourseSelection::getStudentId, sids).eq(CourseSelection::getStatus, 1).select(CourseSelection::getCourseId);
        return courseSelectionMapper.selectList(cw).stream().map(CourseSelection::getCourseId).distinct().collect(Collectors.toList());
    }

    @Override
    public List<String> listAllClassNames() {
        return userMapper.selectList(new LambdaQueryWrapper<User>().eq(User::getRole, "student").isNotNull(User::getClassName).ne(User::getClassName, "").select(User::getClassName))
                .stream().map(User::getClassName).distinct().sorted().collect(Collectors.toList());
    }

    @Override
    public Map<Long, String> getCourseClassMap(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) { return Map.of(); }
        LambdaQueryWrapper<CourseSelection> cw = new LambdaQueryWrapper<>();
        cw.in(CourseSelection::getCourseId, courseIds).eq(CourseSelection::getStatus, 1);
        List<CourseSelection> sels = courseSelectionMapper.selectList(cw);
        if (sels.isEmpty()) { return Map.of(); }
        List<Long> sids = sels.stream().map(CourseSelection::getStudentId).distinct().collect(Collectors.toList());
        Map<Long, User> stuMap = userMapper.selectBatchIds(sids).stream().collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));
        return sels.stream().collect(Collectors.groupingBy(CourseSelection::getCourseId,
                Collectors.mapping(cs -> { User u = stuMap.get(cs.getStudentId()); return u != null ? u.getClassName() : ""; },
                        Collectors.filtering(x -> !x.isEmpty(), Collectors.collectingAndThen(Collectors.toSet(), set -> String.join(",", set))))));
    }

    private Map<Long, List<String>> getCourseClassList(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) { return Map.of(); }
        LambdaQueryWrapper<CourseSelection> cw = new LambdaQueryWrapper<>();
        cw.in(CourseSelection::getCourseId, courseIds).eq(CourseSelection::getStatus, 1);
        List<CourseSelection> sels = courseSelectionMapper.selectList(cw);
        if (sels.isEmpty()) { return Map.of(); }
        List<Long> sids = sels.stream().map(CourseSelection::getStudentId).distinct().collect(Collectors.toList());
        Map<Long, User> stuMap = userMapper.selectBatchIds(sids).stream().collect(Collectors.toMap(User::getId, Function.identity(), (a, b) -> a));
        return sels.stream().collect(Collectors.groupingBy(CourseSelection::getCourseId,
                Collectors.mapping(cs -> { User u = stuMap.get(cs.getStudentId()); return u != null ? u.getClassName() : ""; },
                        Collectors.filtering(x -> x != null && !x.isEmpty(),
                                Collectors.collectingAndThen(Collectors.toSet(), ArrayList::new)))));
    }

    private Map<Long, Integer> getCourseStudentCount(List<Long> courseIds) {
        if (courseIds == null || courseIds.isEmpty()) { return Map.of(); }
        LambdaQueryWrapper<CourseSelection> cw = new LambdaQueryWrapper<>();
        cw.in(CourseSelection::getCourseId, courseIds).eq(CourseSelection::getStatus, 1);
        return courseSelectionMapper.selectList(cw).stream().collect(Collectors.groupingBy(CourseSelection::getCourseId, Collectors.collectingAndThen(Collectors.counting(), Long::intValue)));
    }

    @Override
    public int batchUpdateCourses(List<CourseDTO> dtoList) {
        int count = 0;
        for (CourseDTO dto : dtoList) {
            if (dto.getId() == null) { continue; }
            Course ex = getById(dto.getId());
            if (ex == null) { continue; }
            if (dto.getCode() != null) ex.setCode(dto.getCode());
            if (dto.getName() != null) ex.setName(dto.getName());
            if (dto.getTeacherId() != null) ex.setTeacherId(dto.getTeacherId());
            if (dto.getCredit() != null) ex.setCredit(dto.getCredit());
            if (dto.getHours() != null) ex.setHours(dto.getHours());
            if (dto.getSemester() != null) ex.setSemester(dto.getSemester());
            if (dto.getWeekday() != null) ex.setWeekday(dto.getWeekday());
            if (dto.getStartSection() != null) ex.setStartSection(dto.getStartSection());
            if (dto.getEndSection() != null) ex.setEndSection(dto.getEndSection());
            if (dto.getLocation() != null) ex.setLocation(dto.getLocation());
            if (dto.getMaxStudents() != null) ex.setMaxStudents(dto.getMaxStudents());
            if (dto.getDescription() != null) ex.setDescription(dto.getDescription());
            if (dto.getCourseType() != null) ex.setCourseType(dto.getCourseType());
            updateById(ex);
            count++;
        }
        return count;
    }

    @Override
    public List<ScheduleVO> getTeacherSchedule(Long teacherId, String semester) {
        LambdaQueryWrapper<Course> w = new LambdaQueryWrapper<>();
        w.eq(Course::getTeacherId, teacherId).eq(StringUtils.hasText(semester), Course::getSemester, semester).eq(Course::getStatus, 1);
        List<Course> courses = list(w);
        Map<String, String> roomMap = buildRoomDisplayMap(courses);
        return courses.stream().map(c -> {
            ScheduleVO vo = new ScheduleVO();
            vo.setCourseId(c.getId()); vo.setCourseName(c.getName()); vo.setCode(c.getCode());
            vo.setWeekday(c.getWeekday()); vo.setStartSection(c.getStartSection()); vo.setEndSection(c.getEndSection());
            vo.setLocation(c.getLocation());
            vo.setLocationDisplay(roomMap.getOrDefault(c.getLocation(), c.getLocation()));
            vo.setCredit(c.getCredit()); vo.setHours(c.getHours());
            vo.setCourseType(c.getCourseType());
            vo.setCurrentCount(c.getCurrentCount()); vo.setMaxStudents(c.getMaxStudents());
            User t4 = userMapper.selectById(teacherId);
            vo.setTeacherName(t4 != null ? t4.getRealName() : "");
            LambdaQueryWrapper<CourseSelection> csW = new LambdaQueryWrapper<>();
            csW.eq(CourseSelection::getCourseId, c.getId()).eq(CourseSelection::getStatus, 1);
            List<CourseSelection> sl = courseSelectionMapper.selectList(csW);
            if (!sl.isEmpty()) {
                List<Long> sids2 = sl.stream().map(CourseSelection::getStudentId).distinct().collect(Collectors.toList());
                Map<Long, User> sm2 = userMapper.selectBatchIds(sids2).stream().collect(Collectors.toMap(User::getId, Function.identity()));
                String cls = sl.stream().map(cs -> { User u = sm2.get(cs.getStudentId()); return u != null ? u.getClassName() : null; })
                        .filter(cl -> cl != null && !cl.isEmpty()).distinct().collect(Collectors.joining(","));
                vo.setClassNames(cls);
            }
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public String checkCourseConflict(CourseDTO dto) {
        if (dto.getWeekday() == null || dto.getStartSection() == null || dto.getEndSection() == null) {
            return null;
        }

        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Course::getWeekday, dto.getWeekday())
                .eq(Course::getStatus, 1);
        if (dto.getSemester() != null) {
            wrapper.eq(Course::getSemester, dto.getSemester());
        }
        if (dto.getId() != null) {
            wrapper.ne(Course::getId, dto.getId());
        }
        List<Course> sameTimeCourses = list(wrapper);

        for (Course c : sameTimeCourses) {
            boolean timeOverlap = !(dto.getEndSection() < c.getStartSection() || dto.getStartSection() > c.getEndSection());
            if (!timeOverlap) continue;

            if (dto.getTeacherId() != null && dto.getTeacherId().equals(c.getTeacherId())) {
                User teacher = userMapper.selectById(c.getTeacherId());
                return "教师" + (teacher != null ? teacher.getRealName() : "") + "在该时段已有课程：" + c.getName();
            }

            if (dto.getLocation() != null && dto.getLocation().equals(c.getLocation())) {
                return "教室" + c.getLocation() + "在该时段已被课程占用：" + c.getName();
            }
        }

        return null;
    }

    @Override
    public List<ClassroomVO> listAvailableClassrooms(Integer weekday, Integer startSection, Integer endSection, String semester) {
        if (weekday == null || startSection == null || endSection == null) {
            throw new BusinessException("请先选择上课时间");
        }

        LambdaQueryWrapper<Classroom> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(Classroom::getStatus, 1).orderByAsc(Classroom::getBuilding, Classroom::getName);
        List<Classroom> allRooms = classroomMapper.selectList(roomWrapper);

        LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<>();
        courseWrapper.eq(Course::getWeekday, weekday).eq(Course::getStatus, 1);
        if (StringUtils.hasText(semester)) {
            courseWrapper.eq(Course::getSemester, semester);
        }
        List<Course> courses = list(courseWrapper);

        Set<String> occupiedLocations = courses.stream()
                .filter(c -> c.getLocation() != null)
                .filter(c -> !(endSection < c.getStartSection() || startSection > c.getEndSection()))
                .map(Course::getLocation)
                .collect(Collectors.toSet());

        return allRooms.stream().map(room -> {
            ClassroomVO vo = new ClassroomVO();
            org.springframework.beans.BeanUtils.copyProperties(room, vo);
            boolean isOccupied = occupiedLocations.contains(room.getName());
            vo.setOccupiedRate(isOccupied ? java.math.BigDecimal.ONE : java.math.BigDecimal.ZERO);
            return vo;
        }).collect(Collectors.toList());
    }

    private Map<String, String> buildRoomDisplayMap(List<Course> courses) {
        List<String> locations = courses.stream()
                .map(Course::getLocation)
                .filter(l -> l != null && !l.isEmpty())
                .distinct()
                .collect(Collectors.toList());
        if (locations.isEmpty()) { return Map.of(); }
        LambdaQueryWrapper<Classroom> cw = new LambdaQueryWrapper<Classroom>().in(Classroom::getName, locations);
        List<Classroom> rooms = classroomMapper.selectList(cw);
        return rooms.stream().collect(Collectors.toMap(
                Classroom::getName,
                r -> (r.getBuilding() != null ? r.getBuilding() + " " : "") + r.getName(),
                (a, b) -> a
        ));
    }
}
