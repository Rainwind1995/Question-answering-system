package org.example.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.util.Map;
import java.util.UUID;

/**
 * 工具类
 */
public class CommunityUtil {

    // 生成随机字符串
    // 因为我们使用UUID生成的随机字符串是通过"-"连接起来的,比如: 44e128a5-ac7a-4c9a-be4c-224b6bf81b20
    // 所以我们需要使用replaceALl()将"-"替换掉
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    // MD5加密
    // MD5加密是一个不可逆的过程,并且加密后的内容是固定的,比如: hello -> abc123def456
    // 这种做法存在一定的安全隐患,就是这种固定的加密内容很容易被人试出来,所以我们需要在MD5加密的内容之后添加一个随机字符串
    // 就是我们user表里面的那个salt字段,我么可以 将这个随机字符串添加到hello加密的内容后面变成: hello -> abc123def456yds20
    public static String md5(String key) {
        // StringUtils.isBlank作用是检验字符串类型是否为空,可以校验的有:是否为null、是否为""、是否为空字符串(引号中间有空格)" "
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }


    // FastJson中JSONObject用法及常用方法总结:https://www.cnblogs.com/zjdxr-up/p/9736755.html
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        // 新建JSONObject对象
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {
                json.put(key, map.get(key));
            }
        }
        // JSONObject对象转化为json字符串
        return json.toJSONString();
    }

    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

}
