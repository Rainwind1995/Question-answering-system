package org.example.community.util;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

@Component
public class MailClient {
    // 设置为private是为了防止其他类使用当前类的日志对象；如果当前类需要被子类继承，并且都使用同一个日志对象时，可设置为protected 。
    // 设置为static是为了让每个类中的日志对象只生成一份，日志对象是属于类的，不是属于具体的实例的。
    // 设置成final是为了避免日志对象在运行时被修改
    private static final Logger logger = LoggerFactory.getLogger(MailClient.class);


    // JavaMailSender 在Mail 自动配置类 MailSenderAutoConfiguration 中已经导入，这里直接注入使用即可
    @Autowired
    private JavaMailSender mailSender;

    // 读取配置文件属性值:发件者
    @Value("${spring.mail.username}")
    private String from;

    /**
     *
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     */
    public void sendMail(String to, String subject, String content){
        try {
            // MimeMessage构建邮件
            MimeMessage message = mailSender.createMimeMessage();
            // MimeMessageHelper构建邮件收发信息
            MimeMessageHelper helper = new MimeMessageHelper(message);
            helper.setFrom(from);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(content, true); //true代表邮件内容支持html
            mailSender.send(helper.getMimeMessage());
        } catch (MessagingException e) {
            logger.error("发送邮件失败:" + e.getMessage());
        }
    }

}
