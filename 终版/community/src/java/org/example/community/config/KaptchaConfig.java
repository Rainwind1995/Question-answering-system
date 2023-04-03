package org.example.community.config;

import com.google.code.kaptcha.Producer;
import com.google.code.kaptcha.impl.DefaultKaptcha;
import com.google.code.kaptcha.util.Config;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Properties;

@Configuration
public class KaptchaConfig {

    /**
     *  因为 kaptcha 是一个工具类 没有和Springboot建立联系,所以我们需要自己去配置属性
     * @return
     */
    @Bean
    public Producer kaptchaProducer(){
        Properties properties = new Properties();
        // 设置图片宽度
        properties.setProperty("kaptcha.image.width", "100");
        // 设置图片高度
        properties.setProperty("kaptcha.image.height", "40");
        // 设置图片中文字的大小
        properties.setProperty("kaptcha.textproducer.font.size", "32");
        // 设置文字颜色
        properties.setProperty("kaptcha.textproducer.font.color", "0,0,0");
        // 设置文字
        properties.setProperty("kaptcha.textproducer.char.string", "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ");
        // 设置文字长度
        properties.setProperty("kaptcha.textproducer.char.length", "4");
        // 设置文字没有噪音
        properties.setProperty("kaptcha.noise.impl", "com.google.code.kaptcha.impl.NoNoise");

        DefaultKaptcha kaptcha = new DefaultKaptcha();
        Config config = new Config(properties);
        kaptcha.setConfig(config);
        return kaptcha;
    }
}
