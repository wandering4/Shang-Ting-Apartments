package com.atguigu.lease.web.app.controller.apartment;

import com.atguigu.lease.common.login.LoginUserHolder;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.web.app.service.ApartmentInfoService;
import com.atguigu.lease.web.app.vo.agreement.AgreementItemVo;
import com.atguigu.lease.web.app.vo.apartment.ApartmentDetailVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Tag(name = "公寓信息")
@RequestMapping("/app/apartment")
public class ApartmentController {

    @Autowired
    private ApartmentInfoService service;

    @Operation(summary = "根据id获取公寓信息")
    @GetMapping("getDetailById")
    public Result<ApartmentDetailVo> getDetailById(@RequestParam Long id) {
        ApartmentDetailVo apartmentDetailVo = service.getApartmentDetailById(id);
        return Result.ok(apartmentDetailVo);
    }


    @Operation(summary = "获取方圆n公里内的所有公寓")
    @GetMapping("listApartmentByAddress")
    public Result<List<Long>> listApartment(@RequestParam int n, @RequestParam double latitude, @RequestParam double longitude) {
        List<Long> result=service.listApartmentByAddress(n,latitude,longitude);
        return Result.ok(result);
    }
}
