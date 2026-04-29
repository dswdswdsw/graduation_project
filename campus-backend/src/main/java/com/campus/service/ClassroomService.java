package com.campus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.Classroom;
import com.campus.vo.ClassroomVO;

import java.util.List;

public interface ClassroomService extends IService<Classroom> {

    Page<ClassroomVO> listClassrooms(String building, String type, String keyword, int page, int size);

    List<ClassroomVO> listAvailableClassrooms(Integer weekday, Integer startSection, Integer endSection, String semester);

    void addClassroom(Classroom classroom);

    void updateClassroom(Classroom classroom);

    void deleteClassroom(Long id);

    List<String> listAllBuildings();
}
