package com.campus.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.dto.RepairDTO;
import com.campus.entity.Repair;
import com.campus.vo.RepairVO;

public interface RepairService extends IService<Repair> {

    Page<RepairVO> listRepairs(Long userId, String category, Integer status, int page, int size);

    RepairVO getRepairDetail(Long id);

    void addRepair(RepairDTO dto, Long userId);

    void handleRepair(Long id, Long handlerId, String result);

    void rateRepair(Long id, Long userId, Integer rating, String content);

    Page<RepairVO> listAllRepairs(String category, Integer status, int page, int size);
}
