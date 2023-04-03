package org.example.community.quartz;

import org.example.community.entity.DiscussPost;
import org.example.community.service.DiscussPostService;
import org.example.community.service.ElasticsearchService;
import org.example.community.service.LikeService;
import org.example.community.util.CommunityConstant;
import org.example.community.util.RedisKeyUtil;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundSetOperations;
import org.springframework.data.redis.core.RedisTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class PostScoreRefreshJob implements Job, CommunityConstant {
    private static final Logger logger = LoggerFactory.getLogger(PostScoreRefreshJob.class);

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private DiscussPostService discussPostService;

    @Autowired
    private LikeService likeService;

    @Autowired
    private ElasticsearchService elasticsearchService;

    // 牛客纪元
    private static final Date epoch;

    static{
        try {
            epoch = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse("2014-08-01 00:00:00");
        } catch (ParseException e) {
            throw new RuntimeException("初始化牛客纪元失败!", e);
        }

    }

    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        String redisKey = RedisKeyUtil.getPostScoreKey();
        // 通过 key 获取集合里面的 value 其中 operations 是一个集合
        BoundSetOperations operations = redisTemplate.boundSetOps(redisKey);

        if(operations.size() == 0){
            logger.info("[任务取消] 没有需要刷新的帖子!");
            return ;
        }

        logger.info("[任务开始] 正在刷新帖子分数" + operations.size());
        while(operations.size() > 0){
            // 弹出集合第一个元素
            this.refresh((Integer)operations.pop());
        }

        logger.info("[任务结束] 帖子分数刷新完毕!");

    }

    private void refresh(int postId) {
        // 获取帖子
        DiscussPost post = discussPostService.findDisPostById(postId);

        if(post == null){
            logger.error("该帖子不存在", postId);
            return ;
        }

        // 帖子是否精华
        boolean wonderful = post.getType() == 1 ;

        // 评论数量
        int commentCount = post.getCommentCount();

        // 点赞数量
        long likeCount = likeService.findEntityLikeCount(ENTITY_TYPE_POST, postId);

        // 计算权重
        double w = (wonderful ? 75 : 0) + commentCount * 10 + likeCount * 2;

        // 分数 = 权重 + 距离天数
        double score = Math.log(Math.max(w, 1)) + (post.getCreateTime().getTime() - epoch.getTime()) / (1000 * 3600 * 24);

        // 更新分数
        discussPostService.updateScore(postId, score);

        // 同步搜索数据
        post.setScore(score);
        elasticsearchService.saveDiscussPost(post);
    }
}
