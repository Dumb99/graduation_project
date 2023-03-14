package com.baiyun.asstorage.mapper;

import com.baiyun.asstorage.dto.AsNeighborDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.Collection;
import java.util.List;
import java.util.Map;

@Mapper
public interface AsNeighborMapper extends BaseMapper<AsNeighborDTO> {
    void saveBatchAsNeighbor(List<AsNeighborDTO> list);

    AsNeighborDTO selectByKey(@Param("key") String key);

    List<AsNeighborDTO> selectBatchByKeys(Collection<String> list);

    void updateBatchAsNeighbor(List<AsNeighborDTO> list);

    void updateLocation(List<String> list);

    void updateP2p(@Param("map")Map map);

    void updateP2c(@Param("map")Map map);

    List<AsNeighborDTO> selectBatchAsNeighbor();

    void updateStatusByKeys(List<String> list);
}
