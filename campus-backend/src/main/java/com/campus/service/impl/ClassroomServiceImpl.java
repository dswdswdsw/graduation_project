package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.entity.Classroom;
import com.campus.entity.Course;
import com.campus.exception.BusinessException;
import com.campus.mapper.ClassroomMapper;
import com.campus.mapper.CourseMapper;
import com.campus.service.ClassroomService;
import com.campus.vo.ClassroomVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassroomServiceImpl extends ServiceImpl<ClassroomMapper, Classroom> implements ClassroomService {

    private final CourseMapper courseMapper;

    @Override
    public Page<ClassroomVO> listClassrooms(String building, String type, String keyword, int page, int size) {
        Page<Classroom> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Classroom> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(building)) wrapper.eq(Classroom::getBuilding, building);
        if (StringUtils.hasText(type)) wrapper.eq(Classroom::getType, type);
        if (StringUtils.hasText(keyword)) {
            wrapper.and(w -> w.like(Classroom::getName, keyword).or().like(Classroom::getBuilding, keyword));
        }
        wrapper.eq(Classroom::getStatus, 1).orderByAsc(Classroom::getBuilding, Classroom::getName);
        Page<Classroom> result = page(pageParam, wrapper);

        Page<ClassroomVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(c -> {
            ClassroomVO vo = new ClassroomVO();
            BeanUtils.copyProperties(c, vo);
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public List<ClassroomVO> listAvailableClassrooms(Integer weekday, Integer startSection, Integer endSection, String semester) {
        if (weekday == null || startSection == null || endSection == null) {
            throw new BusinessException("请先选择上课时间");
        }

        LambdaQueryWrapper<Classroom> roomWrapper = new LambdaQueryWrapper<>();
        roomWrapper.eq(Classroom::getStatus, 1).orderByAsc(Classroom::getBuilding, Classroom::getName);
        List<Classroom> allRooms = list(roomWrapper);

        LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<>();
        courseWrapper.eq(Course::getWeekday, weekday)
                .eq(Course::getStatus, 1);
        if (StringUtils.hasText(semester)) {
            courseWrapper.eq(Course::getSemester, semester);
        }
        List<Course> courses = courseMapper.selectList(courseWrapper);

        Set<String> occupiedLocations = courses.stream()
                .filter(c -> c.getLocation() != null)
                .filter(c -> !(endSection < c.getStartSection() || startSection > c.getEndSection()))
                .map(Course::getLocation)
                .collect(Collectors.toSet());

        return allRooms.stream().map(room -> {
            ClassroomVO vo = new ClassroomVO();
            BeanUtils.copyProperties(room, vo);
            boolean isOccupied = occupiedLocations.contains(room.getName())
                    || occupiedLocations.stream().anyMatch(loc -> loc.contains(room.getName()));
            vo.setOccupiedRate(isOccupied ? java.math.BigDecimal.ONE : java.math.BigDecimal.ZERO);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public void addClassroom(Classroom classroom) {
        LambdaQueryWrapper<Classroom> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Classroom::getName, classroom.getName());
        if (count(wrapper) > 0) {
            throw new BusinessException("教室名称已存在");
        }
        classroom.setStatus(1);
        save(classroom);
    }

    @Override
    public void updateClassroom(Classroom classroom) {
        Classroom existing = getById(classroom.getId());
        if (existing == null) throw new BusinessException("教室不存在");
        if (classroom.getName() != null) existing.setName(classroom.getName());
        if (classroom.getBuilding() != null) existing.setBuilding(classroom.getBuilding());
        if (classroom.getCapacity() != null) existing.setCapacity(classroom.getCapacity());
        if (classroom.getType() != null) existing.setType(classroom.getType());
        if (classroom.getFacilities() != null) existing.setFacilities(classroom.getFacilities());
        updateById(existing);
    }

    @Override
    public void deleteClassroom(Long id) {
        Classroom room = getById(id);
        if (room == null) throw new BusinessException("教室不存在");

        LambdaQueryWrapper<Course> courseWrapper = new LambdaQueryWrapper<>();
        courseWrapper.eq(Course::getLocation, room.getName()).eq(Course::getStatus, 1);
        if (courseMapper.selectCount(courseWrapper) > 0) {
            throw new BusinessException("该教室有课程安排，无法删除");
        }
        removeById(id);
    }

    @Override
    public List<String> listAllBuildings() {
        return list(new LambdaQueryWrapper<Classroom>()
                .eq(Classroom::getStatus, 1)
                .select(Classroom::getBuilding)
                .isNotNull(Classroom::getBuilding)
                .groupBy(Classroom::getBuilding))
                .stream().map(Classroom::getBuilding).distinct().sorted().collect(Collectors.toList());
    }
}
