package com.baiyun.asstorage.mapper;

import com.baiyun.asstorage.dto.UserDTO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface UserMapper extends BaseMapper<UserDTO> {
    UserDTO selectByEmail(@Param("email") String email);
}
