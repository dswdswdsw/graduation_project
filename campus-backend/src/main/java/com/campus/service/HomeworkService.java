package com.campus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.dto.HomeworkDTO;
import com.campus.dto.HomeworkGradeDTO;
import com.campus.entity.Homework;
import com.campus.vo.HomeworkGradeVO;
import com.campus.vo.HomeworkSubmissionVO;
import com.campus.vo.HomeworkVO;
import java.util.List;

public interface HomeworkService extends IService<Homework> {

    Page<HomeworkVO> listHomework(Long courseId, Long studentId, Long teacherId, int page, int size);

    HomeworkVO getHomeworkDetail(Long id, Long studentId, Long teacherId);

    void addHomework(HomeworkDTO dto, Long teacherId);

    void updateHomework(HomeworkDTO dto, Long operatorId);

    void deleteHomework(Long id, Long operatorId);

    void submitHomework(Long homeworkId, Long studentId, String content, Long attachmentId);

    Page<HomeworkSubmissionVO> listSubmissions(Long homeworkId, Long teacherId, int page, int size);

    HomeworkGradeVO getGradeDetail(Long homeworkId, Long teacherId);

    List<HomeworkGradeVO> listHomeworkStudents(Long homeworkId, Long teacherId);

    void gradeSubmission(HomeworkGradeDTO dto, Long teacherId);

    void returnSubmission(Long submissionId, String comment, Long teacherId);
}