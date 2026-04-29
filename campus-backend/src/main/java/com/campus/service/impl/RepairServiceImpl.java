package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.dto.RepairDTO;
import com.campus.entity.Repair;
import com.campus.entity.User;
import com.campus.exception.BusinessException;
import com.campus.mapper.RepairMapper;
import com.campus.mapper.UserMapper;
import com.campus.service.RepairService;
import com.campus.vo.RepairVO;
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
public class RepairServiceImpl extends ServiceImpl<RepairMapper, Repair> implements RepairService {

    private final UserMapper userMapper;

    @Override
    public Page<RepairVO> listRepairs(Long userId, String category, Integer status, int page, int size) {
        Page<Repair> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Repair> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(userId != null, Repair::getUserId, userId);
        if (StringUtils.hasText(category)) {
            wrapper.eq(Repair::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(Repair::getStatus, status);
        }
        wrapper.orderByDesc(Repair::getCreateTime);
        Page<Repair> result = page(pageParam, wrapper);
        return convertToRepairVOPage(result);
    }

    @Override
    public RepairVO getRepairDetail(Long id) {
        Repair repair = getById(id);
        if (repair == null) {
            throw new BusinessException("报修工单不存在");
        }
        return convertToRepairVO(repair);
    }

    @Override
    public void addRepair(RepairDTO dto, Long userId) {
        Repair repair = new Repair();
        BeanUtils.copyProperties(dto, repair);
        repair.setUserId(userId);
        repair.setStatus(0);
        if (repair.getUrgency() == null) {
            repair.setUrgency("normal");
        }
        save(repair);
    }

    @Override
    public void handleRepair(Long id, Long handlerId, String result) {
        Repair repair = getById(id);
        if (repair == null) {
            throw new BusinessException("报修工单不存在");
        }
        repair.setHandlerId(handlerId);
        repair.setHandleResult(result);
        repair.setHandleTime(LocalDateTime.now());
        repair.setStatus(2);
        updateById(repair);
    }

    @Override
    public void rateRepair(Long id, Long userId, Integer rating, String content) {
        Repair repair = getById(id);
        if (repair == null) {
            throw new BusinessException("报修工单不存在");
        }
        if (!repair.getUserId().equals(userId)) {
            throw new BusinessException("只能评价自己的报修工单");
        }
        if (repair.getStatus() != 2) {
            throw new BusinessException("只能评价已完成的工单");
        }
        repair.setRating(rating);
        repair.setRatingContent(content);
        repair.setStatus(3);
        updateById(repair);
    }

    @Override
    public Page<RepairVO> listAllRepairs(String category, Integer status, int page, int size) {
        Page<Repair> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<Repair> wrapper = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(category)) {
            wrapper.eq(Repair::getCategory, category);
        }
        if (status != null) {
            wrapper.eq(Repair::getStatus, status);
        }
        wrapper.orderByDesc(Repair::getCreateTime);
        Page<Repair> result = page(pageParam, wrapper);
        return convertToRepairVOPage(result);
    }

    private Page<RepairVO> convertToRepairVOPage(Page<Repair> result) {
        Page<RepairVO> voPage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());

        List<Long> userIds = result.getRecords().stream().map(Repair::getUserId).distinct().collect(Collectors.toList());
        List<Long> handlerIds = result.getRecords().stream()
                .map(Repair::getHandlerId).filter(h -> h != null).distinct().collect(Collectors.toList());
        userIds.addAll(handlerIds);
        Map<Long, User> userMap = userIds.isEmpty() ? Map.of() :
                userMapper.selectBatchIds(userIds.stream().distinct().collect(Collectors.toList()))
                        .stream().collect(Collectors.toMap(User::getId, Function.identity()));

        voPage.setRecords(result.getRecords().stream().map(r -> {
            RepairVO vo = new RepairVO();
            BeanUtils.copyProperties(r, vo);
            User user = userMap.get(r.getUserId());
            if (user != null) vo.setUserName(user.getRealName());
            if (r.getHandlerId() != null) {
                User handler = userMap.get(r.getHandlerId());
                if (handler != null) vo.setHandlerName(handler.getRealName());
            }
            return vo;
        }).collect(Collectors.toList()));
        return voPage;
    }

    private RepairVO convertToRepairVO(Repair repair) {
        RepairVO vo = new RepairVO();
        BeanUtils.copyProperties(repair, vo);
        User user = userMapper.selectById(repair.getUserId());
        if (user != null) vo.setUserName(user.getRealName());
        if (repair.getHandlerId() != null) {
            User handler = userMapper.selectById(repair.getHandlerId());
            if (handler != null) vo.setHandlerName(handler.getRealName());
        }
        return vo;
    }
}
