package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dto.HomeworkDTO;
import com.campus.dto.HomeworkGradeDTO;
import com.campus.entity.Course;
import com.campus.entity.CourseSelection;
import com.campus.entity.Homework;
import com.campus.entity.HomeworkSubmission;
import com.campus.entity.SysFile;
import com.campus.entity.User;
import com.campus.exception.BusinessException;
import com.campus.mapper.CourseMapper;
import com.campus.mapper.CourseSelectionMapper;
import com.campus.mapper.HomeworkMapper;
import com.campus.mapper.HomeworkSubmissionMapper;
import com.campus.mapper.SysFileMapper;
import com.campus.mapper.UserMapper;
import com.campus.service.HomeworkService;
import com.campus.vo.HomeworkGradeVO;
import com.campus.vo.HomeworkSubmissionVO;
import com.campus.vo.HomeworkVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeworkServiceImpl extends ServiceImpl<HomeworkMapper, Homework> implements HomeworkService {

    private final HomeworkSubmissionMapper submissionMapper;
    private final CourseMapper courseMapper;
    private final CourseSelectionMapper courseSelectionMapper;
    private final UserMapper userMapper;
    private final SysFileMapper sysFileMapper;

    private void checkTeacherOwnership(Long homeworkId, Long teacherId) {
        if (teacherId == null) return;
        Homework homework = getById(homeworkId);
        if (homework == null) throw new BusinessException("作业不存在");
        if (!teacherId.equals(homework.getTeacherId())) {
            throw new BusinessException("无权操作此作业");
        }
    }

    @Override
    public Page<HomeworkVO> listHomework(Long courseId, Long studentId, Long teacherId, int page, int size) {
        Page<Homework> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Homework> wrapper = new LambdaQueryWrapper<>();
        if (courseId != null) {
            wrapper.eq(Homework::getCourseId, courseId);
        }
        if (teacherId != null) {
            wrapper.eq(Homework::getTeacherId, teacherId);
        }
        Set<Long> studentCourseIds = null;
        if (studentId != null) {
            LambdaQueryWrapper<CourseSelection> csWrapper = new LambdaQueryWrapper<>();
            csWrapper.eq(CourseSelection::getStudentId, studentId)
                    .eq(CourseSelection::getStatus, 1);
            studentCourseIds = courseSelectionMapper.selectList(csWrapper).stream()
                    .map(CourseSelection::getCourseId).collect(Collectors.toSet());
            if (studentCourseIds.isEmpty()) {
                Page<HomeworkVO> emptyPage = new Page<>(page, size, 0);
                emptyPage.setRecords(List.of());
                return emptyPage;
            }
            wrapper.in(Homework::getCourseId, studentCourseIds);
        }
        wrapper.eq(Homework::getStatus, 1);
        wrapper.orderByDesc(Homework::getCreateTime);
        Page<Homework> result = page(pageParam, wrapper);

        Page<HomeworkVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<Long> courseIds = result.getRecords().stream().map(Homework::getCourseId).distinct().collect(Collectors.toList());
        List<Long> teacherIds = result.getRecords().stream().map(Homework::getTeacherId).distinct().collect(Collectors.toList());
        Map<Long, Course> courseMap = courseIds.isEmpty() ? Map.of() :
                courseMapper.selectBatchIds(courseIds).stream().collect(Collectors.toMap(Course::getId, Function.identity()));
        Map<Long, User> teacherMap = teacherIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(teacherIds).stream().collect(Collectors.toMap(User::getId, Function.identity()));

        List<Long> homeworkIds = result.getRecords().stream().map(Homework::getId).collect(Collectors.toList());
        Map<Long, HomeworkSubmission> submissionMap = Map.of();
        if (studentId != null && !homeworkIds.isEmpty()) {
            LambdaQueryWrapper<HomeworkSubmission> subWrapper = new LambdaQueryWrapper<>();
            subWrapper.eq(HomeworkSubmission::getStudentId, studentId)
                    .in(HomeworkSubmission::getHomeworkId, homeworkIds);
            submissionMap = submissionMapper.selectList(subWrapper).stream()
                    .collect(Collectors.toMap(HomeworkSubmission::getHomeworkId, Function.identity(), (a, b) -> a));
        }
        Map<Long, HomeworkSubmission> finalSubmissionMap = submissionMap;

        Map<Long, String> classNamesMap = Map.of();
        if (!courseIds.isEmpty()) {
            LambdaQueryWrapper<CourseSelection> csWrapper = new LambdaQueryWrapper<>();
            csWrapper.in(CourseSelection::getCourseId, courseIds)
                    .eq(CourseSelection::getStatus, 1);
            List<CourseSelection> selections = courseSelectionMapper.selectList(csWrapper);
            if (!selections.isEmpty()) {
                List<Long> studentIdsForClass = selections.stream().map(CourseSelection::getStudentId).distinct().collect(Collectors.toList());
                Map<Long, User> studentClassMap = userMapper.selectBatchIds(studentIdsForClass).stream()
                        .collect(Collectors.toMap(User::getId, Function.identity()));
                classNamesMap = selections.stream()
                        .collect(Collectors.groupingBy(CourseSelection::getCourseId,
                                Collectors.mapping(cs -> {
                                    User u = studentClassMap.get(cs.getStudentId());
                                    return u != null ? u.getClassName() : null;
                                }, Collectors.filtering(c -> c != null && !c.isEmpty(),
                                        Collectors.collectingAndThen(Collectors.toSet(), set -> String.join(",", set))))));
            }
        }
        Map<Long, String> finalClassNamesMap = classNamesMap;

        Map<Long, Integer> studentCountMap = Map.of();
        if (!courseIds.isEmpty()) {
            LambdaQueryWrapper<CourseSelection> scWrapper = new LambdaQueryWrapper<>();
            scWrapper.in(CourseSelection::getCourseId, courseIds)
                    .eq(CourseSelection::getStatus, 1);
            List<CourseSelection> allSel = courseSelectionMapper.selectList(scWrapper);
            Set<Long> allStudentIds = allSel.stream().map(CourseSelection::getStudentId).collect(Collectors.toSet());
            Set<Long> validStudentIds = allStudentIds.isEmpty() ? Set.of() :
                    userMapper.selectBatchIds(new ArrayList<>(allStudentIds)).stream().map(User::getId).collect(Collectors.toSet());
            studentCountMap = allSel.stream()
                    .filter(cs -> validStudentIds.contains(cs.getStudentId()))
                    .collect(Collectors.groupingBy(CourseSelection::getCourseId,
                            Collectors.mapping(CourseSelection::getStudentId, Collectors.collectingAndThen(Collectors.toSet(), Set::size))));
        }
        Map<Long, Integer> finalStudentCountMap = studentCountMap;

        voPage.setRecords(result.getRecords().stream().map(h -> {
            HomeworkVO vo = new HomeworkVO();
            BeanUtils.copyProperties(h, vo);
            Course course = courseMap.get(h.getCourseId());
            if (course != null) vo.setCourseName(course.getName());
            User teacher = teacherMap.get(h.getTeacherId());
            if (teacher != null) vo.setTeacherName(teacher.getRealName());
            vo.setSubmitted(finalSubmissionMap.containsKey(h.getId()));
            HomeworkSubmission sub = finalSubmissionMap.get(h.getId());
            if (sub != null) {
                vo.setScore(sub.getScore());
                vo.setStatus(sub.getStatus());
            }
            vo.setClassNames(finalClassNamesMap.getOrDefault(h.getCourseId(), ""));
            vo.setStudentCount(finalStudentCountMap.getOrDefault(h.getCourseId(), 0));
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public HomeworkVO getHomeworkDetail(Long id, Long studentId, Long teacherId) {
        Homework homework = getById(id);
        if (homework == null) {
            throw new BusinessException("作业不存在");
        }
        if (teacherId != null && !teacherId.equals(homework.getTeacherId())) {
            throw new BusinessException("无权查看此作业");
        }
        HomeworkVO vo = new HomeworkVO();
        BeanUtils.copyProperties(homework, vo);
        Course course = courseMapper.selectById(homework.getCourseId());
        if (course != null) vo.setCourseName(course.getName());
        User teacher = userMapper.selectById(homework.getTeacherId());
        if (teacher != null) vo.setTeacherName(teacher.getRealName());
        LambdaQueryWrapper<CourseSelection> csWrapper2 = new LambdaQueryWrapper<>();
        csWrapper2.eq(CourseSelection::getCourseId, homework.getCourseId())
                .eq(CourseSelection::getStatus, 1);
        List<CourseSelection> selList = courseSelectionMapper.selectList(csWrapper2);
        if (!selList.isEmpty()) {
            Set<Long> sIdSet = selList.stream().map(CourseSelection::getStudentId).collect(Collectors.toSet());
            Map<Long, User> stuMap = userMapper.selectBatchIds(new ArrayList<>(sIdSet)).stream().collect(Collectors.toMap(User::getId, Function.identity()));
            vo.setStudentCount(stuMap.size());
            String classes = selList.stream()
                    .map(cs -> { User u = stuMap.get(cs.getStudentId()); return u != null ? u.getClassName() : null; })
                    .filter(c -> c != null && !c.isEmpty())
                    .distinct().collect(Collectors.joining(","));
            vo.setClassNames(classes);
        }
        if (studentId != null) {
            LambdaQueryWrapper<HomeworkSubmission> subWrapper = new LambdaQueryWrapper<>();
            subWrapper.eq(HomeworkSubmission::getHomeworkId, id)
                    .eq(HomeworkSubmission::getStudentId, studentId);
            HomeworkSubmission mySub = submissionMapper.selectOne(subWrapper);
            vo.setSubmitted(mySub != null);
            if (mySub != null) {
                HomeworkSubmissionVO subVo = new HomeworkSubmissionVO();
                BeanUtils.copyProperties(mySub, subVo);
                if (mySub.getAttachmentId() != null) {
                    SysFile f = sysFileMapper.selectById(mySub.getAttachmentId());
                    if (f != null) subVo.setAttachmentName(f.getOriginalName());
                }
                vo.setMySubmission(subVo);
            }
        }
        if (homework.getAttachmentId() != null) {
            vo.setAttachmentId(homework.getAttachmentId());
            SysFile f = sysFileMapper.selectById(homework.getAttachmentId());
            if (f != null) vo.setAttachmentName(f.getOriginalName());
        }
        return vo;
    }

    @Override
    public void addHomework(HomeworkDTO dto, Long teacherId) {
        if (dto.getCourseId() != null && teacherId != null) {
            Course course = courseMapper.selectById(dto.getCourseId());
            if (course == null) throw new BusinessException("课程不存在");
            if (!teacherId.equals(course.getTeacherId())) {
                throw new BusinessException("只能为自己教授的课程发布作业");
            }
        }
        Homework homework = new Homework();
        BeanUtils.copyProperties(dto, homework);
        homework.setTeacherId(teacherId);
        homework.setSubmitCount(0);
        homework.setStatus(1);
        if (homework.getTotalScore() == null) {
            homework.setTotalScore(new java.math.BigDecimal("100"));
        }
        save(homework);
    }

    @Override
    public void updateHomework(HomeworkDTO dto, Long operatorId) {
        Homework homework = getById(dto.getId());
        if (homework == null) {
            throw new BusinessException("作业不存在");
        }
        if (operatorId != null && !operatorId.equals(homework.getTeacherId())) {
            throw new BusinessException("无权编辑此作业");
        }
        if (dto.getTitle() != null) homework.setTitle(dto.getTitle());
        if (dto.getDescription() != null) homework.setDescription(dto.getDescription());
        if (dto.getDeadline() != null) homework.setDeadline(dto.getDeadline());
        if (dto.getTotalScore() != null) homework.setTotalScore(dto.getTotalScore());
        updateById(homework);
    }

    @Override
    public void deleteHomework(Long id, Long operatorId) {
        if (operatorId != null) {
            checkTeacherOwnership(id, operatorId);
        }
        removeById(id);
    }

    @Override
    public void submitHomework(Long homeworkId, Long studentId, String content, Long attachmentId) {
        Homework homework = getById(homeworkId);
        if (homework == null) {
            throw new BusinessException("作业不存在");
        }
        if (homework.getDeadline() != null && LocalDateTime.now().isAfter(homework.getDeadline())) {
            throw new BusinessException("作业已过截止时间");
        }

        LambdaQueryWrapper<HomeworkSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HomeworkSubmission::getHomeworkId, homeworkId)
                .eq(HomeworkSubmission::getStudentId, studentId);
        HomeworkSubmission existing = submissionMapper.selectOne(wrapper);

        if (existing != null) {
            existing.setContent(content);
            existing.setAttachmentId(attachmentId);
            existing.setSubmitTime(LocalDateTime.now());
            submissionMapper.updateById(existing);
        } else {
            HomeworkSubmission submission = new HomeworkSubmission();
            submission.setHomeworkId(homeworkId);
            submission.setStudentId(studentId);
            submission.setContent(content);
            submission.setAttachmentId(attachmentId);
            submission.setStatus(0);
            submission.setSubmitTime(LocalDateTime.now());
            submissionMapper.insert(submission);

            homework.setSubmitCount(homework.getSubmitCount() + 1);
            updateById(homework);
        }
    }

    @Override
    public Page<HomeworkSubmissionVO> listSubmissions(Long homeworkId, Long teacherId, int page, int size) {
        if (teacherId != null) {
            checkTeacherOwnership(homeworkId, teacherId);
        }
        Page<HomeworkSubmission> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<HomeworkSubmission> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(HomeworkSubmission::getHomeworkId, homeworkId)
                .orderByDesc(HomeworkSubmission::getSubmitTime);
        Page<HomeworkSubmission> result = submissionMapper.selectPage(pageParam, wrapper);

        Page<HomeworkSubmissionVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<Long> studentIds = result.getRecords().stream().map(HomeworkSubmission::getStudentId).distinct().collect(Collectors.toList());
        Map<Long, User> studentMap = studentIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(studentIds).stream().collect(Collectors.toMap(User::getId, Function.identity()));

        voPage.setRecords(result.getRecords().stream().map(s -> {
            HomeworkSubmissionVO vo = new HomeworkSubmissionVO();
            BeanUtils.copyProperties(s, vo);
            User student = studentMap.get(s.getStudentId());
            if (student != null) {
                vo.setStudentName(student.getRealName());
                vo.setUserNo(student.getUserNo());
            }
            if (s.getAttachmentId() != null) {
                SysFile f = sysFileMapper.selectById(s.getAttachmentId());
                if (f != null) vo.setAttachmentName(f.getOriginalName());
            }
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public void gradeSubmission(HomeworkGradeDTO dto, Long teacherId) {
        HomeworkSubmission submission = submissionMapper.selectById(dto.getSubmissionId());
        if (submission == null) {
            throw new BusinessException("提交记录不存在");
        }
        if (submission.getStatus() != null && submission.getStatus() == 2) {
            throw new BusinessException("该作业已打回，学生重新提交后才能批改");
        }
        if (teacherId != null) {
            Homework homework = getById(submission.getHomeworkId());
            if (homework == null || !teacherId.equals(homework.getTeacherId())) {
                throw new BusinessException("无权批改此作业");
            }
        }
        submission.setScore(dto.getScore());
        submission.setComment(dto.getComment());
        submission.setStatus(1);
        submission.setGradeTime(LocalDateTime.now());
        submissionMapper.updateById(submission);
    }

    @Override
    public HomeworkGradeVO getGradeDetail(Long homeworkId, Long teacherId) {
        Homework homework = getById(homeworkId);
        if (homework == null) throw new BusinessException("作业不存在");
        if (teacherId != null && !teacherId.equals(homework.getTeacherId())) {
            throw new BusinessException("无权查看此作业");
        }
        Course course = courseMapper.selectById(homework.getCourseId());
        HomeworkGradeVO detail = new HomeworkGradeVO();
        detail.setHomeworkId(homeworkId);
        detail.setTitle(homework.getTitle());
        detail.setCourseName(course != null ? course.getName() : "");
        detail.setDeadline(String.valueOf(homework.getDeadline()));
        detail.setTotalScore(homework.getTotalScore());
        return detail;
    }

    @Override
    public List<HomeworkGradeVO> listHomeworkStudents(Long homeworkId, Long teacherId) {
        Homework homework = getById(homeworkId);
        if (homework == null) throw new BusinessException("作业不存在");
        if (teacherId != null && !teacherId.equals(homework.getTeacherId())) {
            throw new BusinessException("无权查看此作业");
        }
        LambdaQueryWrapper<CourseSelection> csWrapper = new LambdaQueryWrapper<>();
        csWrapper.eq(CourseSelection::getCourseId, homework.getCourseId())
                .eq(CourseSelection::getStatus, 1);
        List<CourseSelection> selections = courseSelectionMapper.selectList(csWrapper);

        List<Long> studentIds = selections.stream().map(CourseSelection::getStudentId).distinct().collect(Collectors.toList());
        Map<Long, User> studentMap = studentIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(studentIds).stream().collect(Collectors.toMap(User::getId, Function.identity()));

        LambdaQueryWrapper<HomeworkSubmission> subWrapper = new LambdaQueryWrapper<>();
        subWrapper.eq(HomeworkSubmission::getHomeworkId, homeworkId);
        Map<Long, HomeworkSubmission> submissionMap = submissionMapper.selectList(subWrapper).stream()
                .collect(Collectors.toMap(HomeworkSubmission::getStudentId, Function.identity(), (a, b) -> a));

        return studentIds.stream().map(studentId -> {
            HomeworkGradeVO vo = new HomeworkGradeVO();
            vo.setHomeworkId(homeworkId);
            vo.setStudentId(studentId);
            User student = studentMap.get(studentId);
            if (student != null) {
                vo.setStudentName(student.getRealName());
                vo.setUserNo(student.getUserNo());
                vo.setClassName(student.getClassName());
            }
            HomeworkSubmission sub = submissionMap.get(studentId);
            if (sub != null) {
                vo.setSubmitted(true);
                vo.setContent(sub.getContent());
                vo.setAttachmentId(sub.getAttachmentId());
                if (sub.getAttachmentId() != null) {
                    SysFile f = sysFileMapper.selectById(sub.getAttachmentId());
                    if (f != null) vo.setAttachmentName(f.getOriginalName());
                }
                vo.setScore(sub.getScore());
                vo.setStatus(sub.getStatus());
                vo.setSubmitTime(sub.getSubmitTime());
                vo.setId(sub.getId());
            } else {
                vo.setSubmitted(false);
            }
            return vo;
        }).filter(vo -> vo.getStudentName() != null).collect(Collectors.toList());
    }

    @Override
    public void returnSubmission(Long submissionId, String comment, Long teacherId) {
        HomeworkSubmission submission = submissionMapper.selectById(submissionId);
        if (submission == null) {
            throw new BusinessException("提交记录不存在");
        }
        if (teacherId != null) {
            Homework homework = getById(submission.getHomeworkId());
            if (homework == null || !teacherId.equals(homework.getTeacherId())) {
                throw new BusinessException("无权操作此作业");
            }
        }
        submission.setStatus(2);
        submission.setComment(comment);
        submission.setScore(null);
        submission.setGradeTime(LocalDateTime.now());
        submissionMapper.updateById(submission);
    }
}