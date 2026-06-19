package com.itsean.campus_second_hand.controller.user;

import com.itsean.campus_second_hand.entity.AddressBook;
import com.itsean.campus_second_hand.entity.result.Result;
import com.itsean.campus_second_hand.service.AddressBookService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/address")
@Slf4j
@Api(tags = "地址簿接口")
public class AddressBookController {

    @Autowired
    private AddressBookService addressBookService;

    /**
     * 新增地址
     * @param addressBook
     * @return
     */
    @PostMapping("/add")
    @ApiOperation("新增地址")
    public Result add(@RequestBody AddressBook addressBook){
        log.info("新增地址：{}", addressBook);
        addressBookService.add(addressBook);
        return Result.success("添加成功");
    }

    /**
     * 查询地址
     * @param id
     * @return
     */
    @GetMapping("/{id}")
    @ApiOperation("查询地址")
    public Result<AddressBook> getById(@PathVariable Long id){
        log.info("查询id为{}的地址", id);
        AddressBook addressBook = addressBookService.getById(id);
        return Result.success(addressBook);
    }

    /**
     * 修改地址
     * @param addressBook
     * @return
     */
    @ApiOperation("修改地址")
    @PutMapping("/update")
    public Result update(@RequestBody AddressBook addressBook){
        log.info("修改地址：{}",addressBook);
        addressBookService.update(addressBook);
        return Result.success("修改成功");
    }

    /**
     * 删除地址
     * @param id
     * @return
     */
    @ApiOperation("删除地址")
    @DeleteMapping("/delete/{id}")
    public Result delete(@PathVariable Long id){
        log.info("删除id为{}的地址", id);
        addressBookService.delete(id);
        return Result.success("删除成功");
    }


}
