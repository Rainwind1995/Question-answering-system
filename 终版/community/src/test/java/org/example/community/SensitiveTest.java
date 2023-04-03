package org.example.community;

import org.example.community.util.SensitiveFilter;
import org.junit.Test;
import org.junit.runner.RunWith;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SensitiveTest {

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Test
    public void sensitiveTest(){
        String text = "疫情当下,你们能生活在中国,你就偷着乐吧";
        String text1 = "疫情当下,强歼你们能生活在中国,你就偷着乐吧";
        String text2 = "bbbccfabc";
        text = sensitiveFilter.filter(text);
        text1 = sensitiveFilter.filter(text1);
        text2 = sensitiveFilter.filter(text2);
        System.out.println(text);
        System.out.println(text1);
        System.out.println(text2);

    }



}
