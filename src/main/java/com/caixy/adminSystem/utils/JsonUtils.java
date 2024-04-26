package com.caixy.adminSystem.utils;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Json操作类
 *
 * @name: com.caixy.project.utils.JsonUtils
 * @author: CAIXYPROMISE
 * @since: 2023-12-29 19:49
 **/
/**
 * Json操作类
 *
 * @name: com.caixy.project.utils.JsonUtils
 * @author: CAIXYPROMISE
 * @since: 2023-12-29 19:49
 **/
public class JsonUtils
{
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd HH:mm:ss")
            .setLongSerializationPolicy(LongSerializationPolicy.STRING)
            .serializeNulls().create();


    /**
     * 将 JSON 字符串转换为 Map
     *
     * @param json      JSON 字符串
     * @param keyType   键的类型
     * @param valueType 值的类型
     * @param <K>       键的泛型
     * @param <V>       值的泛型
     * @return 转换后的 Map
     */
    public static <K, V> Map<K, V> jsonToMap(String json, Class<K> keyType, Class<V> valueType)
    {
        Type type = TypeToken.getParameterized(Map.class, keyType, valueType).getType();
        return gson.fromJson(json, type);
    }

    public static HashMap<String, Object> jsonToMap(String json)
    {
        Type mapType = new TypeToken<HashMap<String, String>>()
        {
        }.getType();
        return gson.fromJson(json, mapType);
    }

    /**
     * map转json
     *
     * @author CAIXYPROMISE
     * @version a
     * @since 2024/16 15:52
     */
    public static String mapToString(Map<?, ?> map)
    {
        return gson.toJson(map);
    }

    public static String toJsonString(Object object)
    {
        return gson.toJson(object);
    }

    /**
     * 将 JSON 字符串转换为对象列表
     *
     * @param json    JSON 字符串
     * @param <T>     对象类型
     * @return 对象列表
     */
    public static <T> List<T> jsonToList(String json)
    {
        return jsonToObject(json, new TypeToken<List<T>>(){}.getType());
    }

    /**
     * Json转对象
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/4/26 下午6:56
     */
    public static <T> T jsonToObject(String json, Class<T> targetType)
    {
        return gson.fromJson(json, targetType);
    }

    public static <T> T jsonToObject(String json, Type typeOfT)
    {
        return gson.fromJson(json, typeOfT);
    }

    /**
     * 尝试修复非Json格式的字符串成Json对象并返回String
     * @param incorrectJson 非json格式的字符串
     * @return Json对象字符串
     *
     * @author CAIXYPROMISE
     * @version 1.0
     * @since 2024/4/26 下午6:55
     */
    public static String fixedJson(String incorrectJson)
    {
        JsonObject jsonObject = null;
        try {
            jsonObject = gson.fromJson(incorrectJson, JsonObject.class);
        } catch (JsonSyntaxException e) {
            System.out.println("Error: " + e.getMessage());
        }
        return gson.toJson(jsonObject);
    }
}
