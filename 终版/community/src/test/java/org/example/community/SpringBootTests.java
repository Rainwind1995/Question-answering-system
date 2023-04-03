package org.example.community;


import org.example.community.entity.DiscussPost;
import org.example.community.service.DiscussPostService;
import org.junit.*;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class SpringBootTests {

    @Autowired
    private DiscussPostService discussPostService;

    private DiscussPost data;

    // 在类中只会被执行一次
    @BeforeClass
    public static void beforeClass(){
        System.out.println("'beforeClass'");
    }

    // 所有测试用例执行完才执行一次
    @AfterClass
    public static void afterClass(){
        System.out.println("afterClass");
    }

    // 在跑测试的时候都会各执行一次@Before部分的代码
    @Before
    public void before(){
        System.out.println("before");

        // 初始化测试数据
        data = new DiscussPost();
        data.setUserId(111);
        data.setTitle("单元测试");
        data.setContent("这是一个单元测试呀");
        data.setCreateTime(new Date());
        data.setScore(200.00);
        discussPostService.addDiscussPost(data);
    }


    // 释放资源  对于每一个测试方法都要执行一次
    @After
    public void after(){
        System.out.println("after");

        // 删除数据
        discussPostService.updateStatus(data.getId(), 2);
    }

    @Test
    public void  test1(){
        System.out.println("test1");
    }

    @Test
    public void  test2(){
        System.out.println("test2");
    }

    @Test
    public void testFindById(){
        DiscussPost post = discussPostService.findDisPostById(data.getId());
        Assert.assertNotNull(post);
        Assert.assertEquals(data.getTitle(), post.getTitle());
    }

    @Test
    public void testUpdateScore(){
        int row = discussPostService.updateScore(data.getId(), 2000.00);
        Assert.assertEquals(1, row);
        DiscussPost post = discussPostService.findDisPostById(data.getId());
        Assert.assertEquals(2000.00, post.getScore(), 2);
    }




}
