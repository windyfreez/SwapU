package com.itsean.campus_second_hand.service.Impl;

import com.itsean.campus_second_hand.context.BaseContext;
import com.itsean.campus_second_hand.entity.AddressBook;
import com.itsean.campus_second_hand.mapper.AddressBookMapper;
import com.itsean.campus_second_hand.service.AddressBookService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressBookServiceImpl implements AddressBookService {

    @Autowired
    private AddressBookMapper addressBookMapper;

    /**
     * 新增地址
     * @param addressBook
     */
    @Override
    public void add(AddressBook addressBook) {
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookMapper.insert(addressBook);
    }

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    @Override
    public AddressBook getById(Long id) {
        AddressBook addressBook = addressBookMapper.getById(id);
        return addressBook;
    }

    /**
     * 修改地址
     * @param addressBook
     */
    @Override
    public void update(AddressBook addressBook) {
        addressBookMapper.update(addressBook);
    }

    /**
     * 删除地址
     * @param id
     */
    @Override
    public void delete(Long id) {
        addressBookMapper.delete(id);
    }

    /**
     * 设置默认地址
     * @param addressBook
     */
    @Override
    @Transactional
    public void setDefault(AddressBook addressBook) {
        //将当前用户的其他地址设为非默认
        addressBook.setIsDefault(0);
        addressBook.setUserId(BaseContext.getCurrentId());
        addressBookMapper.updateIsDefaultByUserId(addressBook);

        //将当前地址设为默认
        addressBook.setIsDefault(1);
        addressBookMapper.update(addressBook);
    }

    /**
     * 根据用户id查询地址
     * @param currentId
     * @return
     */
    @Override
    public List<AddressBook> listByUserId(Long currentId) {
        List<AddressBook> list = addressBookMapper.listByUserId(currentId);
        return list;
    }
}
