package com.campus.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.campus.entity.DictData;

import java.util.List;

public interface DictDataService extends IService<DictData> {

    List<DictData> listByTypeCode(String typeCode);
}
