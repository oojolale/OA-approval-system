package com.OA.system.service.impl;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.OA.system.entity.UserInfoEntity;
import com.OA.system.mapper.UserInfoMapper;
import com.OA.system.service.UserInfoService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserInfoServiceImpl extends ServiceImpl<UserInfoMapper, UserInfoEntity> implements UserInfoService {

}
