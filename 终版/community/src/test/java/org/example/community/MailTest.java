package org.example.community;


import org.example.community.util.CommunityUtil;
import org.example.community.util.MailClient;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class MailTest {

    @Autowired
    private MailClient mailClient;

    @Test
    public void testTextMail(){
        mailClient.sendMail("920744043@qq.com", "标题乱码", "收到请回复下,我是通过soul认识你的,想加个好友");
    }

    @Test
    public void getJSONString(){
        System.out.println(CommunityUtil.getJSONString(0, null, null));
    }

}
