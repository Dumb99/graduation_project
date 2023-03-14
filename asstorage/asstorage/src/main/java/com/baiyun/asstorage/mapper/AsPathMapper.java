package com.baiyun.asstorage.mapper;

import com.baiyun.asstorage.dto.AsPathDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;
import java.util.Map;

@Mapper
public interface AsPathMapper extends BaseMapper<AsPathDTO> {

    List<AsPathDTO> selectByStatus();

    void updateStatusById(List<Long> list);

    void resetStatus();

    Map statusCount();
}
