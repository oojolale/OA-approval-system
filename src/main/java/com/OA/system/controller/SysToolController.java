package com.OA.system.controller;

import cn.hutool.core.util.ObjectUtil;
import com.github.yulichang.query.MPJQueryWrapper;
import com.OA.system.entity.LocationRequest;
import com.OA.system.entity.UserInfoEntity;
import com.OA.system.service.CurrentUserService;
import com.OA.system.service.LocationRequestService;
import com.OA.system.service.UserInfoService;
import com.OA.system.util.AjaxResult;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.*;

@RestController
@RequestMapping
public class SysToolController {
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private LocationRequestService locationRequestService;
    @Autowired
    CurrentUserService currentUserService;

    @GetMapping("userinfo")
    public AjaxResult userinfo(){
        MPJQueryWrapper<UserInfoEntity> queryWrapper = new MPJQueryWrapper<>();
        queryWrapper.select("t.*").leftJoin("roles on roles.organization_id = t.id");
        queryWrapper.eq("t.id",1);
        List<Map<String, Object>> maps = userInfoService.listMaps(queryWrapper);
        Map<String, Object> map = new HashMap<>();
        map.put("userinfo",maps);
        return AjaxResult.success(map);
    }

    @GetMapping("api/hello")
    public Map<String, Object> apiHello(HttpServletResponse response){
        Map<String, Object> user = currentUserService.getUser();
        Map<String, Object> map = new HashMap<>();
        if(ObjectUtil.isNotNull(user)){
            map.put("user",user.get("user"));
            map.put("authorities",user.get("authorities"));
        }
        return map;
    }

    @PostMapping("api/uploadLocation")
    public AjaxResult uploadLocation(@RequestBody LocationRequest locationRequest, HttpServletResponse response) {
        MPJQueryWrapper<LocationRequest> locationRequestMPJQueryWrapper = new MPJQueryWrapper<>();
        boolean save = locationRequestService.save(locationRequest);
        Map<String, Object> map = new HashMap<>();
        map.put("record",save);
        return AjaxResult.success(map);
    }

    @PostMapping("api/uploadFiles")
    public AjaxResult uploadLocation(MultipartFile[] file) throws IOException {
        for (int i = 0; i < file.length; i++) {
            file[i].transferTo(new File(Objects.requireNonNull(file[i].getOriginalFilename())));
        }
        return AjaxResult.success(null);
    }
}

