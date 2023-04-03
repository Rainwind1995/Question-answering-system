package org.example.community;


import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisStringCommands;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)
public class RedisTest {

    @Autowired
    private RedisTemplate redisTemplate;

    @Test
    public void testStrings() {
        String redisKey = "test:count";

        redisTemplate.opsForValue().set(redisKey, 1);

        System.out.println(redisTemplate.opsForValue().get(redisKey));
        System.out.println(redisTemplate.opsForValue().increment(redisKey));
        System.out.println(redisTemplate.opsForValue().decrement(redisKey));
    }


    @Test
    public void testHashes(){
        String redisKey = "test:user";

        redisTemplate.opsForHash().put(redisKey, "id", 1);
        redisTemplate.opsForHash().put(redisKey, "username", "aa");


        System.out.println(redisTemplate.opsForHash().get(redisKey, "id"));
        System.out.println(redisTemplate.opsForHash().get(redisKey, "username"));
    }

    @Test
    public void testLists(){
        String redisKey = "test:ids";

        // 添加
        redisTemplate.opsForList().leftPush(redisKey, 10);
        redisTemplate.opsForList().leftPush(redisKey, 11);
        redisTemplate.opsForList().leftPush(redisKey, 12);

        // 输出 list 的大小
        System.out.println(redisTemplate.opsForList().size(redisKey));
        // 输出 list 的全部元素
        System.out.println(redisTemplate.opsForList().range(redisKey, 0, -1));

    }


    @Test
    public void testSets(){
        String redisKey = "test:teacher";

        redisTemplate.opsForSet().add(redisKey, "张三", "李四", "王五", "赵六");

        System.out.println(redisTemplate.opsForSet().size(redisKey));
        // 打印全部成员
        System.out.println(redisTemplate.opsForSet().members(redisKey));
        // 读取第一个元素
        System.out.println(redisTemplate.opsForSet().pop(redisKey));
    }


    @Test
    public void testSortedSets() {
//        String redisKey = "test:students";
//
//        redisTemplate.opsForZSet().add(redisKey, "唐僧", 80);
//        redisTemplate.opsForZSet().add(redisKey, "悟空", 90);
//        redisTemplate.opsForZSet().add(redisKey, "八戒", 50);
//        redisTemplate.opsForZSet().add(redisKey, "沙僧", 70);
//        redisTemplate.opsForZSet().add(redisKey, "白龙马", 60);
//
//
//        // 获取集合大小
//        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
//        System.out.println(redisTemplate.opsForZSet().score(redisKey, "八戒"));
//        System.out.println(redisTemplate.opsForZSet().reverseRank(redisKey, "八戒"));
//        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey, 0, 2));
//        System.out.println("哈哈哈哈");

        String redisKey = "test:stu";
        redisTemplate.opsForZSet().add(redisKey, "张三", 2022);
        redisTemplate.opsForZSet().add(redisKey, "李四", 2023);
        redisTemplate.opsForZSet().add(redisKey, "王五", 2020);
        redisTemplate.opsForZSet().add(redisKey, "赵六", 2021);

        // 按照分数逆序 从高到低 输出区间内的成员
        System.out.println(redisTemplate.opsForZSet().reverseRange(redisKey, 0, -1));
        // 获取有序集合的成员数
        System.out.println(redisTemplate.opsForZSet().zCard(redisKey));
        // 获取指定元素的分数: 返回的 Double 类型
        System.out.println(redisTemplate.opsForZSet().score(redisKey, "张三"));
        //

    }


    // 统计20万个重复数据的独立总数
    @Test
    public void testHyperLogLog(){
        String redisKey = "test:hll:01";

        // 先添加 10w 条数据
        for(int i = 1; i <= 100000; i++){
            redisTemplate.opsForHyperLogLog().add(redisKey, i);
        }

        // 随机生成 1 - 100000 数据
        for(int i = 1; i <= 100000; i++){
            int r = (int) (Math.random()*100000 + 1);
            redisTemplate.opsForHyperLogLog().add(redisKey, r);
        }

        long size = redisTemplate.opsForHyperLogLog().size(redisKey);
        System.out.println(size);
    }


    // 将三组数据合并 再统计合并后的重复数据的独立总数.
    @Test
    public void testHyperLogUnion(){
        String redisKey2 = "test:hll:02";
        for(int i = 1; i <= 10000; i++){
            redisTemplate.opsForHyperLogLog().add(redisKey2, i);
        }

        String redisKey3 = "test:hll:03";
        for(int i = 5001; i <= 15000; i++){
            redisTemplate.opsForHyperLogLog().add(redisKey3, i);
        }

        String redisKey4 = "test:hll:04";
        for(int i = 10001; i <= 20000; i++){
            redisTemplate.opsForHyperLogLog().add(redisKey4, i);
        }

        String unionKey = "test:hll:union";
        redisTemplate.opsForHyperLogLog().union(unionKey, redisKey2, redisKey3, redisKey4);

        long size = redisTemplate.opsForHyperLogLog().size(unionKey);
        System.out.println(size);

    }


    // 统计一组数据的布尔值
    @Test
    public void testBitMap(){
        String redisKey = "test:bm:01";

        // 记录
        redisTemplate.opsForValue().setBit(redisKey, 1, true);
        redisTemplate.opsForValue().setBit(redisKey, 4, true);
        redisTemplate.opsForValue().setBit(redisKey, 7, true);

        // 查询
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));

        // 统计
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                return connection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(obj);

    }


    // 统计3组数据的布尔值, 并对这3组数据做OR运算.
    @Test
    public void testBitMapOperation(){
        String redisKey2 = "test:bm:02";
        redisTemplate.opsForValue().setBit(redisKey2, 0, true);
        redisTemplate.opsForValue().setBit(redisKey2, 1, true);
        redisTemplate.opsForValue().setBit(redisKey2, 2, true);

        String redisKey3 = "test:bm:03";
        redisTemplate.opsForValue().setBit(redisKey3, 2, true);
        redisTemplate.opsForValue().setBit(redisKey3, 3, true);
        redisTemplate.opsForValue().setBit(redisKey3, 4, true);

        String redisKey4 = "test:bm:04";
        redisTemplate.opsForValue().setBit(redisKey3, 4, true);
        redisTemplate.opsForValue().setBit(redisKey3, 5, true);
        redisTemplate.opsForValue().setBit(redisKey3, 6, true);

        String redisKey = "test:bm:or";
        Object obj = redisTemplate.execute(new RedisCallback() {
            @Override
            public Object doInRedis(RedisConnection connection) throws DataAccessException {
                connection.bitOp(RedisStringCommands.BitOperation.OR, redisKey.getBytes(), redisKey2.getBytes(), redisKey3.getBytes(), redisKey4.getBytes());
                return connection.bitCount(redisKey.getBytes());
            }
        });

        System.out.println(obj);
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 0));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 1));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 2));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 3));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 4));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 5));
        System.out.println(redisTemplate.opsForValue().getBit(redisKey, 6));
    }


}
