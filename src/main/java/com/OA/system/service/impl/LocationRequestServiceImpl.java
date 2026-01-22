package com.OA.system.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.OA.system.entity.LocationRequest;
import com.OA.system.mapper.LocationRequestMapper;
import com.OA.system.service.LocationRequestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class LocationRequestServiceImpl extends ServiceImpl<LocationRequestMapper, LocationRequest> implements LocationRequestService {
}
