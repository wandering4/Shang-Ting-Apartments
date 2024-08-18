package com.atguigu.lease.web.admin.custom.converter;


import com.atguigu.lease.model.enums.ItemType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

//@Component
public class StringToItemTypeConverter implements Converter<String, ItemType> {
    @Override
    public ItemType convert(String source) {

        ItemType[] values = ItemType.class.getEnumConstants();
        for (ItemType itemType : values) {
            if (itemType.getCode().equals(Integer.valueOf(source))) {
                return itemType;
            }
        }

        throw new IllegalArgumentException("code:"+source+"非法");
    }
}
