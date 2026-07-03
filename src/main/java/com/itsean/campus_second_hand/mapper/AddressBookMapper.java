package com.itsean.campus_second_hand.mapper;

import com.itsean.campus_second_hand.entity.AddressBook;
import org.apache.ibatis.annotations.*;

@Mapper
public interface AddressBookMapper {

    /**
     * 新增地址
     * @param addressBook
     */
    @Insert("insert into address_book " +
            "(user_id, consignee, phone, sex, province_code, province_name, city_code, city_name, district_code, district_name, detail, label, is_default) " +
            "values (#{userId}, #{consignee}, #{phone}, #{sex}, #{provinceCode}, #{provinceName}, #{cityCode}, #{cityName}, #{districtCode}, #{districtName}, #{detail}, #{label}, #{isDefault})")
    void insert(AddressBook addressBook);

    /**
     * 根据id查询地址
     * @param id
     * @return
     */
    @Select("select * from address_book where id = #{id}")
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
    @Delete("delete from address_book where id = #{id}")
    void delete(Long id);

    /**
     * 修改用户默认地址
     * @param addressBook
     */
    @Update("update address_book set is_default = 0 where user_id = #{userId}")
    void updateIsDefaultByUserId(AddressBook addressBook);
}
