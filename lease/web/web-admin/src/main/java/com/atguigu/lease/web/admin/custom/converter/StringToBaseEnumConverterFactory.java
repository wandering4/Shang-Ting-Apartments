package com.atguigu.lease.web.admin.custom.converter;

import com.atguigu.lease.model.enums.BaseEnum;
import com.atguigu.lease.model.enums.ItemType;
import org.springframework.core.convert.converter.Converter;
import org.springframework.core.convert.converter.ConverterFactory;
import org.springframework.stereotype.Component;

@Component
public class StringToBaseEnumConverterFactory implements ConverterFactory<String, BaseEnum> {
    @Override
    public <T extends BaseEnum> Converter<String, T> getConverter(Class<T> targetType) {
        return new Converter<String, T>() {
            @Override
            public T convert(String source) {

                T[] values = targetType.getEnumConstants();
                for (T t : values) {
                    if (t.getCode().equals(Integer.valueOf(source))) {
                        return t;
                    }
                }
                throw new IllegalArgumentException("非法的枚举值:"+source);
            }
        };
    }
}
