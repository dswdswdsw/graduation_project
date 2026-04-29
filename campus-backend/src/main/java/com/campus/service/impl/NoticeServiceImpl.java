package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dto.NoticeDTO;
import com.campus.entity.Notice;
import com.campus.entity.NoticeRead;
import com.campus.entity.User;
import com.campus.exception.BusinessException;
import com.campus.mapper.NoticeMapper;
import com.campus.mapper.NoticeReadMapper;
import com.campus.mapper.UserMapper;
import com.campus.service.NoticeService;
import com.campus.vo.NoticeVO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class NoticeServiceImpl extends ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    private final NoticeReadMapper noticeReadMapper;
    private final UserMapper userMapper;

    @Override
    public Page<NoticeVO> listNotices(String category, String targetRole, Long userId, int page, int size) {
        Page<Notice> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Notice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Notice::getStatus, 1);
        if (StringUtils.hasText(category)) {
            wrapper.eq(Notice::getCategory, category);
        }
        if (StringUtils.hasText(targetRole)) {
            if ("admin".equals(targetRole)) {
            } else if ("teacher".equals(targetRole)) {
                wrapper.and(w -> w.eq(Notice::getTargetRole, "all")
                        .or().eq(Notice::getTargetRole, "teacher")
                        .or().eq(Notice::getTargetRole, "student"));
            } else {
                wrapper.and(w -> w.eq(Notice::getTargetRole, "all")
                        .or().eq(Notice::getTargetRole, targetRole));
            }
        }
        wrapper.orderByDesc(Notice::getIsTop).orderByDesc(Notice::getPublishTime);
        Page<Notice> result = page(pageParam, wrapper);

        Page<NoticeVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        List<Long> publisherIds = result.getRecords().stream().map(Notice::getPublisherId).distinct().collect(Collectors.toList());
        Map<Long, User> publisherMap = publisherIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(publisherIds).stream().collect(Collectors.toMap(User::getId, Function.identity()));

        List<Long> noticeIds = result.getRecords().stream().map(Notice::getId).collect(Collectors.toList());
        Map<Long, NoticeRead> readMap = Map.of();
        if (userId != null && !noticeIds.isEmpty()) {
            LambdaQueryWrapper<NoticeRead> readWrapper = new LambdaQueryWrapper<>();
            readWrapper.eq(NoticeRead::getUserId, userId).in(NoticeRead::getNoticeId, noticeIds);
            readMap = noticeReadMapper.selectList(readWrapper).stream()
                    .collect(Collectors.toMap(NoticeRead::getNoticeId, Function.identity(), (a, b) -> a));
        }
        Map<Long, NoticeRead> finalReadMap = readMap;

        voPage.setRecords(result.getRecords().stream().map(n -> {
            NoticeVO vo = new NoticeVO();
            BeanUtils.copyProperties(n, vo);
            User publisher = publisherMap.get(n.getPublisherId());
            if (publisher != null) vo.setPublisherName(publisher.getRealName());
            vo.setIsRead(finalReadMap.containsKey(n.getId()));
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    @Override
    public NoticeVO getNoticeDetail(Long id, Long userId) {
        Notice notice = getById(id);
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        NoticeVO vo = new NoticeVO();
        BeanUtils.copyProperties(notice, vo);
        User publisher = userMapper.selectById(notice.getPublisherId());
        if (publisher != null) vo.setPublisherName(publisher.getRealName());

        if (userId != null) {
            LambdaQueryWrapper<NoticeRead> readWrapper = new LambdaQueryWrapper<>();
            readWrapper.eq(NoticeRead::getNoticeId, id).eq(NoticeRead::getUserId, userId);
            vo.setIsRead(noticeReadMapper.selectCount(readWrapper) > 0);
        }
        return vo;
    }

    @Override
    public void addNotice(NoticeDTO dto, Long publisherId) {
        Notice notice = new Notice();
        BeanUtils.copyProperties(dto, notice);
        notice.setPublisherId(publisherId);
        notice.setStatus(1);
        if (notice.getIsTop() == null) notice.setIsTop(0);
        if (notice.getTargetRole() == null) notice.setTargetRole("all");
        if (notice.getCategory() == null) notice.setCategory("general");
        if (notice.getPublishTime() == null) {
            notice.setPublishTime(LocalDateTime.now());
        }
        save(notice);
    }

    @Override
    public void updateNotice(NoticeDTO dto) {
        Notice notice = getById(dto.getId());
        if (notice == null) {
            throw new BusinessException("公告不存在");
        }
        if (dto.getTitle() != null) notice.setTitle(dto.getTitle());
        if (dto.getContent() != null) notice.setContent(dto.getContent());
        if (dto.getCategory() != null) notice.setCategory(dto.getCategory());
        if (dto.getTargetRole() != null) notice.setTargetRole(dto.getTargetRole());
        updateById(notice);
    }

    @Override
    public void deleteNotice(Long id) {
        removeById(id);
    }

    @Override
    public void markAsRead(Long noticeId, Long userId) {
        LambdaQueryWrapper<NoticeRead> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NoticeRead::getNoticeId, noticeId).eq(NoticeRead::getUserId, userId);
        if (noticeReadMapper.selectCount(wrapper) == 0) {
            NoticeRead read = new NoticeRead();
            read.setNoticeId(noticeId);
            read.setUserId(userId);
            noticeReadMapper.insert(read);
        }
    }

    @Override
    public Long getUnreadCount(Long userId, String role) {
        LambdaQueryWrapper<Notice> noticeWrapper = new LambdaQueryWrapper<>();
        noticeWrapper.eq(Notice::getStatus, 1);
        if (StringUtils.hasText(role)) {
            if ("admin".equals(role)) {
            } else if ("teacher".equals(role)) {
                noticeWrapper.and(w -> w.eq(Notice::getTargetRole, "all")
                        .or().eq(Notice::getTargetRole, "teacher")
                        .or().eq(Notice::getTargetRole, "student"));
            } else {
                noticeWrapper.and(w -> w.eq(Notice::getTargetRole, "all")
                        .or().eq(Notice::getTargetRole, role));
            }
        }
        List<Notice> notices = list(noticeWrapper);
        if (notices.isEmpty()) return 0L;

        List<Long> noticeIds = notices.stream().map(Notice::getId).collect(Collectors.toList());
        LambdaQueryWrapper<NoticeRead> readWrapper = new LambdaQueryWrapper<>();
        readWrapper.eq(NoticeRead::getUserId, userId).in(NoticeRead::getNoticeId, noticeIds);
        return (long) noticeIds.size() - noticeReadMapper.selectCount(readWrapper);
    }
}
