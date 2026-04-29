package com.campus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.dto.NoticeDTO;
import com.campus.entity.Notice;
import com.campus.vo.NoticeVO;

public interface NoticeService extends IService<Notice> {

    Page<NoticeVO> listNotices(String category, String targetRole, Long userId, int page, int size);

    NoticeVO getNoticeDetail(Long id, Long userId);

    void addNotice(NoticeDTO dto, Long publisherId);

    void updateNotice(NoticeDTO dto);

    void deleteNotice(Long id);

    void markAsRead(Long noticeId, Long userId);

    Long getUnreadCount(Long userId, String role);
}
