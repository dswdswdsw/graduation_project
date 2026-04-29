package com.campus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.dto.CourseDTO;
import com.campus.entity.Course;
import com.campus.vo.AdminScheduleVO;
import com.campus.vo.CourseVO;
import com.campus.vo.ScheduleVO;

import com.campus.vo.ClassroomVO;

import java.util.List;
import java.util.Map;

public interface CourseService extends IService<Course> {

    Page<CourseVO> listCourses(String semester, String keyword, int page, int size);

    Page<AdminScheduleVO> listAdminSchedule(String semester, String className, Long teacherId, String courseType, String keyword, int page, int size);

    List<String> listAllClassNames();

    Map<Long, String> getCourseClassMap(List<Long> courseIds);

    CourseVO getCourseDetail(Long id);

    void addCourse(CourseDTO dto);

    void updateCourse(CourseDTO dto);

    int batchUpdateCourses(List<CourseDTO> dtoList);

    void deleteCourse(Long id);

    List<ScheduleVO> getStudentSchedule(Long studentId, String semester);

    List<ScheduleVO> getTeacherSchedule(Long teacherId, String semester);

    List<ClassroomVO> listAvailableClassrooms(Integer weekday, Integer startSection, Integer endSection, String semester);

    String checkCourseConflict(CourseDTO dto);
}
