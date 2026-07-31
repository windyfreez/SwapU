package com.itsean.campus_second_hand.service;

import com.itsean.campus_second_hand.entity.AddressBook;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface AddressBookService{

    /**
     * 新增地址
     * @param addressBook
     */
    void add(AddressBook addressBook);

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    AddressBook getById(Long id);

    /**
     * 修改地址
     * @param addressBook
     */
    void update(AddressBook addressBook);

    /**
     * 删除地址
     * @param id
     */
    void delete(Long id);

    /**
     * 设置默认地址
     * @param addressBook
     */
    void setDefault(AddressBook addressBook);

    /**
     * 根据用户id查询地址
     * @param currentId
     * @return
     */
    List<AddressBook> listByUserId(Long currentId);
}
