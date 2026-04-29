package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dto.ScoreDTO;
import com.campus.entity.Course;
import com.campus.entity.CourseSelection;
import com.campus.entity.Score;
import com.campus.entity.ScoreChangeLog;
import com.campus.entity.User;
import com.campus.exception.BusinessException;
import com.campus.mapper.CourseMapper;
import com.campus.mapper.CourseSelectionMapper;
import com.campus.mapper.ScoreChangeLogMapper;
import com.campus.mapper.ScoreMapper;
import com.campus.mapper.UserMapper;
import com.campus.service.ScoreService;
import com.campus.vo.ScoreStatisticsVO;
import com.campus.vo.ScoreVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, Score> implements ScoreService {

    private final UserMapper userMapper;
    private final CourseMapper courseMapper;
    private final CourseSelectionMapper courseSelectionMapper;
    private final ScoreChangeLogMapper scoreChangeLogMapper;

    @Override
    public Page<ScoreVO> listStudentScores(Long studentId, String semester, int page, int size) {
        Page<Score> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Score::getStudentId, studentId);
        if (StringUtils.hasText(semester)) {
            wrapper.eq(Score::getSemester, semester);
        }
        wrapper.orderByDesc(Score::getCreateTime);
        Page<Score> result = page(pageParam, wrapper);
        return convertToScoreVOPage(result);
    }

    @Override
    public Page<ScoreVO> listCourseScores(Long courseId, String semester, int page, int size) {
        Page<Score> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Score::getCourseId, courseId);
        if (StringUtils.hasText(semester)) {
            wrapper.eq(Score::getSemester, semester);
        }
        wrapper.orderByDesc(Score::getCreateTime);
        Page<Score> result = page(pageParam, wrapper);
        return convertToScoreVOPage(result);
    }

    @Override
    @Transactional
    public void addScore(ScoreDTO dto, Long teacherId) {
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Score::getStudentId, dto.getStudentId())
                .eq(Score::getCourseId, dto.getCourseId())
                .eq(Score::getSemester, dto.getSemester());
        if (count(wrapper) > 0) {
            throw new BusinessException("该学生此课程成绩已存在");
        }

        Score score = new Score();
        BeanUtils.copyProperties(dto, score);
        score.setTeacherId(teacherId);
        calculateFinalScore(score);
        save(score);
    }

    @Override
    @Transactional
    public void updateScore(ScoreDTO dto, Long teacherId) {
        Score score = getById(dto.getId());
        if (score == null) {
            throw new BusinessException("成绩记录不存在");
        }

        BigDecimal oldFinal = score.getFinalScore();

        if (dto.getUsualScore() != null) score.setUsualScore(dto.getUsualScore());
        if (dto.getExamScore() != null) score.setExamScore(dto.getExamScore());
        if (dto.getRemark() != null) score.setRemark(dto.getRemark());
        calculateFinalScore(score);
        updateById(score);

        ScoreChangeLog changeLog = new ScoreChangeLog();
        changeLog.setScoreId(score.getId());
        changeLog.setOperatorId(teacherId);
        changeLog.setOldScore(oldFinal);
        changeLog.setNewScore(score.getFinalScore());
        changeLog.setReason(dto.getRemark());
        scoreChangeLogMapper.insert(changeLog);
    }

    @Override
    public ScoreStatisticsVO getStudentStatistics(Long studentId, String semester) {
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Score::getStudentId, studentId);
        if (StringUtils.hasText(semester)) {
            wrapper.eq(Score::getSemester, semester);
        }
        List<Score> scores = list(wrapper);
        return calculateStatistics(scores);
    }

    @Override
    public ScoreStatisticsVO getCourseStatistics(Long courseId, String semester) {
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Score::getCourseId, courseId);
        if (StringUtils.hasText(semester)) {
            wrapper.eq(Score::getSemester, semester);
        }
        List<Score> scores = list(wrapper);
        return calculateStatistics(scores);
    }

    private void calculateFinalScore(Score score) {
        if (score.getUsualScore() != null && score.getExamScore() != null) {
            BigDecimal usual = score.getUsualScore();
            BigDecimal exam = score.getExamScore();
            BigDecimal finalScore = usual.multiply(new BigDecimal("0.3")).add(exam.multiply(new BigDecimal("0.7")))
                    .setScale(2, RoundingMode.HALF_UP);
            score.setFinalScore(finalScore);

            if (finalScore.compareTo(new BigDecimal("90")) >= 0) {
                score.setGradePoint(new BigDecimal("4.0"));
            } else if (finalScore.compareTo(new BigDecimal("85")) >= 0) {
                score.setGradePoint(new BigDecimal("3.7"));
            } else if (finalScore.compareTo(new BigDecimal("82")) >= 0) {
                score.setGradePoint(new BigDecimal("3.3"));
            } else if (finalScore.compareTo(new BigDecimal("78")) >= 0) {
                score.setGradePoint(new BigDecimal("3.0"));
            } else if (finalScore.compareTo(new BigDecimal("75")) >= 0) {
                score.setGradePoint(new BigDecimal("2.7"));
            } else if (finalScore.compareTo(new BigDecimal("72")) >= 0) {
                score.setGradePoint(new BigDecimal("2.3"));
            } else if (finalScore.compareTo(new BigDecimal("68")) >= 0) {
                score.setGradePoint(new BigDecimal("2.0"));
            } else if (finalScore.compareTo(new BigDecimal("64")) >= 0) {
                score.setGradePoint(new BigDecimal("1.5"));
            } else if (finalScore.compareTo(new BigDecimal("60")) >= 0) {
                score.setGradePoint(new BigDecimal("1.0"));
            } else {
                score.setGradePoint(new BigDecimal("0"));
            }
        }
    }

    @Override
    public List<ScoreVO> listCourseStudentsForEntry(Long courseId) {
        LambdaQueryWrapper<CourseSelection> csW = new LambdaQueryWrapper<>();
        csW.eq(CourseSelection::getCourseId, courseId).eq(CourseSelection::getStatus, 1);
        List<CourseSelection> selections = courseSelectionMapper.selectList(csW);
        if (selections.isEmpty()) return List.of();
        List<Long> studentIds = selections.stream().map(CourseSelection::getStudentId).distinct().collect(Collectors.toList());
        Map<Long, User> studentMap = userMapper.selectBatchIds(studentIds).stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));
        LambdaQueryWrapper<Score> sW = new LambdaQueryWrapper<>();
        sW.eq(Score::getCourseId, courseId).in(Score::getStudentId, studentIds);
        List<Score> scores = list(sW);
        Map<String, Score> scoreMap = scores.stream().collect(
                Collectors.toMap(sc -> sc.getStudentId() + "_" + sc.getCourseId(), Function.identity()));
        return selections.stream().map(cs -> {
            ScoreVO vo = new ScoreVO();
            User u = studentMap.get(cs.getStudentId());
            if (u != null) {
                vo.setStudentId(u.getId());
                vo.setStudentName(u.getRealName());
                vo.setUserNo(u.getUserNo());
                vo.setClassName(u.getClassName());
            }
            vo.setCourseId(courseId);
            String key = cs.getStudentId() + "_" + courseId;
            Score existing = scoreMap.get(key);
            if (existing != null) {
                vo.setId(existing.getId());
                vo.setUsualScore(existing.getUsualScore());
                vo.setExamScore(existing.getExamScore());
                vo.setFinalScore(existing.getFinalScore());
            }
            return vo;
        }).collect(Collectors.toList());
    }

    private ScoreStatisticsVO calculateStatistics(List<Score> scores) {
        ScoreStatisticsVO vo = new ScoreStatisticsVO();
        if (scores.isEmpty()) {
            return vo;
        }
        List<BigDecimal> finals = scores.stream()
                .map(Score::getFinalScore)
                .filter(s -> s != null)
                .collect(Collectors.toList());
        if (finals.isEmpty()) return vo;

        BigDecimal sum = finals.stream().reduce(BigDecimal.ZERO, BigDecimal::add);
        vo.setAvgScore(sum.divide(new BigDecimal(finals.size()), 2, RoundingMode.HALF_UP));
        vo.setMaxScore(finals.stream().max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        vo.setMinScore(finals.stream().min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
        vo.setTotalStudents(scores.size());

        long passCount = finals.stream().filter(s -> s.compareTo(new BigDecimal("60")) >= 0).count();
        vo.setPassRate(new BigDecimal(passCount).divide(new BigDecimal(finals.size()), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));

        long excellentCount = finals.stream().filter(s -> s.compareTo(new BigDecimal("90")) >= 0).count();
        vo.setExcellentRate(new BigDecimal(excellentCount).divide(new BigDecimal(finals.size()), 4, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100")).setScale(2, RoundingMode.HALF_UP));

        BigDecimal totalGpa = BigDecimal.ZERO;
        BigDecimal totalCredits = BigDecimal.ZERO;
        for (Score s : scores) {
            if (s.getGradePoint() != null) {
                Course course = courseMapper.selectById(s.getCourseId());
                if (course != null) {
                    totalGpa = totalGpa.add(s.getGradePoint().multiply(course.getCredit()));
                    totalCredits = totalCredits.add(course.getCredit());
                }
            }
        }
        vo.setTotalCredits(totalCredits);
        if (totalCredits.compareTo(BigDecimal.ZERO) > 0) {
            vo.setGpa(totalGpa.divide(totalCredits, 2, RoundingMode.HALF_UP));
        }
        return vo;
    }

    private Page<ScoreVO> convertToScoreVOPage(Page<Score> result) {
        Page<ScoreVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());

        List<Long> studentIds = result.getRecords().stream().map(Score::getStudentId).distinct().collect(Collectors.toList());
        List<Long> courseIds = result.getRecords().stream().map(Score::getCourseId).distinct().collect(Collectors.toList());
        List<Long> teacherIds = result.getRecords().stream().map(Score::getTeacherId).distinct().collect(Collectors.toList());

        Map<Long, User> studentMap = studentIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(studentIds).stream().collect(Collectors.toMap(User::getId, Function.identity()));
        Map<Long, Course> courseMap = courseIds.isEmpty() ? Map.of() :
                courseMapper.selectBatchIds(courseIds).stream().collect(Collectors.toMap(Course::getId, Function.identity()));
        Map<Long, User> teacherMap = teacherIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(teacherIds).stream().collect(Collectors.toMap(User::getId, Function.identity()));

        voPage.setRecords(result.getRecords().stream().map(s -> {
            ScoreVO vo = new ScoreVO();
            BeanUtils.copyProperties(s, vo);
            User student = studentMap.get(s.getStudentId());
            if (student != null) {
                vo.setStudentName(student.getRealName());
                vo.setUserNo(student.getUserNo());
            }
            Course course = courseMap.get(s.getCourseId());
            if (course != null) vo.setCourseName(course.getName());
            User teacher = teacherMap.get(s.getTeacherId());
            if (teacher != null) vo.setTeacherName(teacher.getRealName());
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }
}
