package org.example.community.service;

import org.example.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    @Autowired
    private RedisTemplate redisTemplate;

    /**
     *  点赞
     * @param userId
     * @param entityType
     * @param entityId
     */
    public void like(int userId, int entityType, int entityId, int entityUserId){
//        // 设置 key
//        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
//        // 判断集合中是否存在 key-value
//        boolean isMember = redisTemplate.opsForSet().isMember(entityLikeKey, userId);
//        if(isMember){
//            // 存在, 说明已经点赞过, 再次点击就是取消点赞
//            redisTemplate.opsForSet().remove(entityLikeKey, userId);
//        } else {
//            // 不存在, 说明没有点赞过, 再次点击就是点赞
//            redisTemplate.opsForSet().add(entityLikeKey, userId);
//        }
        redisTemplate.execute(new SessionCallback() {
            @Override
            public Object execute(RedisOperations operations) throws DataAccessException {
                // 设置点赞的 key
                String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
                // 设置收到的赞的 key
                String userLikeKey = RedisKeyUtil.getUserLikeKey(entityUserId);
                // 查询需要放到事务执行过程之外,  如果放到事务执行过程之类 事务不会立即执行 会存放到队列
                boolean isMember = operations.opsForSet().isMember(entityLikeKey, userId);
                // 开启事务
                operations.multi();

                if(isMember){
                    // 当前用户已点赞, 在次点击则为取消点赞
                    operations.opsForSet().remove(entityLikeKey, userId);
                    // 用户收到的点赞数减一
                    operations.opsForValue().decrement(userLikeKey);
                } else {
                    // 当前用户未被点赞, 再次点击则为点赞
                    operations.opsForSet().add(entityLikeKey, userId);
                    // 用户收到的点赞数加一
                    operations.opsForValue().increment(userLikeKey);
                }
                // 执行事务
                return operations.exec();
            }
        });

    }

    /**
     * 查询某实体获赞数量
     * @param entityType
     * @param entityId
     * @return
     */
    public long findEntityLikeCount(int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        return redisTemplate.opsForSet().size(entityLikeKey);
    }


    /**
     * 查询某人对某个实体的点赞状态
     * @param userId
     * @param entityType
     * @param entityId
     * @return
     */
    public int findEntityLikeStatus(int userId, int entityType, int entityId){
        String entityLikeKey = RedisKeyUtil.getEntityLikeKey(entityType, entityId);
        // 这里我们不选择返回boolean 因为后续还有其他状态的话 true 和 false 不好表示
        // 这里如果true 则返回1 否则返回 0
        return redisTemplate.opsForSet().isMember(entityLikeKey, userId) ? 1 : 0;
    }


    /**
     *  查询用户收到的点赞数
     * @param userId
     * @return
     */
    public int findUserLikeCount(int userId){
        String userLikeKey = RedisKeyUtil.getUserLikeKey(userId);
        Integer count = (Integer) redisTemplate.opsForValue().get(userLikeKey);
        return count == null ? 0 : count.intValue();
    }




}
