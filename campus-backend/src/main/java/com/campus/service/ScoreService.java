package com.campus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.dto.ScoreDTO;
import com.campus.entity.Score;
import com.campus.vo.ScoreStatisticsVO;
import com.campus.vo.ScoreVO;

import java.util.List;

public interface ScoreService extends IService<Score> {

    Page<ScoreVO> listStudentScores(Long studentId, String semester, int page, int size);

    Page<ScoreVO> listCourseScores(Long courseId, String semester, int page, int size);

    void addScore(ScoreDTO dto, Long teacherId);

    void updateScore(ScoreDTO dto, Long teacherId);

    ScoreStatisticsVO getStudentStatistics(Long studentId, String semester);

    ScoreStatisticsVO getCourseStatistics(Long courseId, String semester);

    List<ScoreVO> listCourseStudentsForEntry(Long courseId);
}
