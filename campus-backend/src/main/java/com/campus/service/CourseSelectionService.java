package com.campus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.CourseSelection;
import com.campus.vo.MyCourseVO;

import java.util.List;

public interface CourseSelectionService extends IService<CourseSelection> {

    void selectCourse(Long studentId, Long courseId);

    void dropCourse(Long studentId, Long courseId);

    Page<MyCourseVO> listMySelections(Long studentId, String semester, int page, int size);

    int batchAssignToClass(Long courseId, String className);

    List<String> listAllClasses();
}
