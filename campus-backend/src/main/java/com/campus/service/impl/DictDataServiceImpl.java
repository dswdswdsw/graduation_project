package com.campus.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.campus.entity.DictData;
import com.campus.entity.DictType;
import com.campus.mapper.DictDataMapper;
import com.campus.mapper.DictTypeMapper;
import com.campus.service.DictDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DictDataServiceImpl extends ServiceImpl<DictDataMapper, DictData> implements DictDataService {

    private final DictTypeMapper dictTypeMapper;

    @Override
    public List<DictData> listByTypeCode(String typeCode) {
        LambdaQueryWrapper<DictType> typeWrapper = new LambdaQueryWrapper<>();
        typeWrapper.eq(DictType::getTypeCode, typeCode);
        DictType dictType = dictTypeMapper.selectOne(typeWrapper);
        if (dictType == null) {
            return List.of();
        }
        LambdaQueryWrapper<DictData> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(DictData::getTypeId, dictType.getId())
                .eq(DictData::getStatus, 1)
                .orderByAsc(DictData::getSort);
        return list(wrapper);
    }
}
