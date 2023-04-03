package org.example.community.service;

import org.example.community.entity.DiscussPost;
import org.example.community.entity.User;
import org.example.community.util.CommunityConstant;
import org.example.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CollectService implements CommunityConstant {
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private DiscussPostService discussPostService;

    // 关注
    public void collect(int userId, int entityType, int entityId) {
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                String collectKey = RedisKeyUtil.getCollectKey(userId, entityType);
                String collectedKey = RedisKeyUtil.getCollectedKey(entityType, entityId);
                Double isMember = redisTemplate.opsForZSet().score(collectKey, entityId);

                operations.multi();

                if (isMember != null) {
                    // 如果已经收藏,再次点击则为取消收藏
                    operations.opsForZSet().remove(collectKey, entityId);
                    operations.opsForZSet().remove(collectedKey, userId);

                } else {
                    // 如果没有收藏再次点击则为收藏
                    operations.opsForZSet().add(collectKey, entityId, System.currentTimeMillis());
                    operations.opsForZSet().add(collectedKey, userId, System.currentTimeMillis());
                }

                return operations.exec();
            }
        });
    }


    // 查询某个实体被收藏的次数
    public long findCollectedCount(int entityType, int entityId) {
        String collectedKey = RedisKeyUtil.getCollectedKey(entityType, entityId);
        return redisTemplate.opsForZSet().zCard(collectedKey);
    }

    // 查询用户收藏帖子的数量
    public long findCollectCount(int userId, int entityType){
        String collectKey = RedisKeyUtil.getCollectKey(userId, entityType);
        return redisTemplate.opsForZSet().zCard(collectKey);
    }

    // 查询某个实体是否被收藏
    public int hasCollected(int userId, int entityType, int entityId) {
        String collectKey = RedisKeyUtil.getCollectKey(userId, entityType);
        if (redisTemplate.opsForZSet().score(collectKey, entityId) == null) return 0;
        return 1;
    }


    // 查询用户收藏的帖子
    public List<Map<String, Object>> findCollect(int userId, int offset, int limit){
        String collectKey = RedisKeyUtil.getCollectKey(userId, ENTITY_TYPE_POST);
        Set<Integer> postIds = redisTemplate.opsForZSet().reverseRange(collectKey, offset, offset + limit - 1);

        if(postIds == null) return null;

        List<Map<String, Object>> list = new ArrayList<>();

        for(Integer postId : postIds){
            Map<String, Object> map = new HashMap<>();
            DiscussPost post = discussPostService.findDisPostById(postId);
            map.put("post", post);
            Double score = redisTemplate.opsForZSet().score(collectKey, postId);
            map.put("collectTime", new Date(score.longValue()));
            list.add(map);
        }

        return list;
    }


}
