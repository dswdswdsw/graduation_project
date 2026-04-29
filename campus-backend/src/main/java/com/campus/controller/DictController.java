package com.campus.controller;

import com.campus.common.Result;
import com.campus.entity.DictData;
import com.campus.service.DictDataService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dict")
@RequiredArgsConstructor
public class DictController {

    private final DictDataService dictDataService;

    @GetMapping("/{typeCode}")
    public Result<List<DictData>> listByTypeCode(@PathVariable String typeCode) {
        return Result.success(dictDataService.listByTypeCode(typeCode));
    }
}
