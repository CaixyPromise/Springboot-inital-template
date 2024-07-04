package com.caixy.adminSystem.model.common;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.io.Serializable;

/**
 * 选项VO
 *
 * @author CAIXYPROMISE
 * @name com.caixy.adminSystem.model.common.OptionVO
 * @since 2024-06-17 20:06
 **/
@Data
@AllArgsConstructor
public class OptionVO<T> implements Serializable
{
    /**
     * 模板选项所选中的value
     */
    private T value;
    /**
     * 选项label
     */
    private String label;

    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    public T getValue()
    {
        return value;
    }
}
