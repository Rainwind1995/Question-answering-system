package org.example.community.util;

public class RedisKeyUtil {
    private static final String SPLIT = ":";
    private static final String PREFIX_ENTITY_LIKE = "like:entity";
    private static final String PREFIX_USER_LIKE = "like:user";
    private static final String PREFIX_FOLLOWEE = "followee";
    private static final String PREFIX_FOLLOWER = "follower";
    private static final String PREFIX_COLLECT = "collect";
    private static final String PREFIX_COLLECTED = "collected";
    private static final String PREFIX_KAPTCHA = "kaptcha";
    private static final String PREFIX_TICKET = "ticket";
    private static final String PREFIX_USER = "user";
    private static final String PREFIX_UV = "uv";
    private static final String PREFIX_DAU = "dau";
    private static final String PREFIX_POST = "post";

    // 某个实体的赞
    // like:entity:entityType:entityId
    public static String getEntityLikeKey(int entityType, int entityId){
        return PREFIX_ENTITY_LIKE + SPLIT + entityType + SPLIT + entityId;
    }

    // 某个用户收到的赞
    // like:user:userId
    public static String getUserLikeKey(int userId){
        return PREFIX_USER_LIKE + SPLIT + userId;
    }


    // 某个用户关注的实体 -> (实体ID, 当前时间)
    // followee:userId:entityType -> zset(entityId, now)
    public static String getFolloweeKey(int userId, int entityType){
        return PREFIX_FOLLOWEE + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体拥有的粉丝 -> (用户ID, 当前时间)
    // follower:entityType:entityId -> zset(userId, now)
    public static String getFollowerKey(int entityType, int entityId){
        return PREFIX_FOLLOWER + SPLIT + entityType + SPLIT + entityId;
    }


    // 某个用户收藏的实体
    public static String getCollectKey(int userId, int entityType){
        return PREFIX_COLLECT + SPLIT + userId + SPLIT + entityType;
    }

    // 某个实体被收藏的次数
    public static String getCollectedKey(int entityType, int entityId){
        return PREFIX_COLLECTED + SPLIT + entityType + SPLIT + entityId;
    }

//    // 某个实体被收藏的次数
//    public static String getCollectKey(int entityType, int entityId){
//        return PREFIX_COLLECT + SPLIT + entityType + SPLIT + entityId;
//    }
//
//    // 某个实体获得的收藏数 (collect:post:userId)
//    public static String getEntityCollectKey(int userId){
//        return PREFIX_POST_COLLECT + SPLIT + userId;
//    }



    // 验证码
    public static String getKaptchaKey(String owner){
        return PREFIX_KAPTCHA + SPLIT + owner;
    }

    // 登录凭证
    public static String getTicketKey(String ticket){
        return PREFIX_TICKET + SPLIT + ticket;
    }

    // 用户
    public static String getUserKey(int userId){
        return PREFIX_USER + SPLIT + userId;
    }

    // 单日 UV
    public static String getUVKey(String Date){
        return PREFIX_UV + SPLIT + Date;
    }

    // 区间 UV
    public static String getUVKey(String startDate, String endDate){
        return PREFIX_UV + SPLIT + startDate + SPLIT + endDate;
    }

    // 当日活跃用户
    public static String getDAUKey(String Date){
        return PREFIX_DAU + SPLIT + Date;
    }

    // 区间活跃用户
    public static String getDAUKey(String startDate, String endDate){
        return PREFIX_DAU + SPLIT+ startDate + SPLIT + endDate;
    }

    // 帖子分数
    public static String getPostScoreKey(){
        return PREFIX_POST + SPLIT + "score";
    }

}
