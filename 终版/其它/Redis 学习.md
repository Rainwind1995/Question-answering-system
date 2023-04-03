> Redis 学习

```
下载地址: https://github.com/microsoftarchive/redis/releases
redis 官方文档: https://redis.io/
安装redis可视化工具 Another Redis DeskTop Manager:https://goanother.com/cn/
```



> SpringBoot 整合 Redis

```
去官网找相关依赖: https://mvnrepository.com/
在 pom.xml 里面添加下面这段代码:
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-redis</artifactId>
    <version>2.1.6.RELEASE</version>
</dependency>
```

```
在 application.properites 编写 ReidsProperties 属性设置
编写 RedisConfig 配置类重写 redisTemplate 进行相关的设置
```



> Redis 实现点赞功能

```
实现点赞需要使到的三个方法为: 
like(): 主要实现点赞, 通过 redis 中的 set 来添加或者删除点赞数
findEntityLikeCount(): 查找当前帖子的点赞数
findEntityLikeStatus(): 查找当前帖子的点赞状态
实现 RedisKeyUtil 来定义一个统一前缀的 key
例如: like:entity:2:90
like:entity 是统一的前缀, 2 代表的是回复, 90 代表的是回复 id 
```

```
前端显示点赞需要注意的一些问题
如果我们没有登录访问帖子列表, 我们查看到的是当前帖子或者回复显示的应该是 赞 + 数字 
而不是 已赞 + 数字
所以我们需要在 likeStatus 那里设置 hostHolder.getUser == null 返回 0 前端显示的就是赞
```

```
问题一: 创建 discuss.js 实现 like() 函数里面 jquery 的text（）方法设置的时候出现乱码
解决方案:
主要是我创建的 discuss.js 这个文件的编码不是 UTF-8 导致的, 所以只需要修改下这个文件的编码格式就行
参考文献: https://bbs.csdn.net/topics/390764205
```



> Redis 实现我收到的赞功能

```
添加一个 userLikeKey 统计每个用户对应的点赞个数: like:user:userId
因为单纯的使用 entityLikeKey 无法单独获取到用户收到的点赞数
我们还需要重写 likeService 里面的 like() 方法 需要使用到事务 (明天查资料看看)
```



> Redis 实现关注、取消关注

```
实现用户关注的实体
关注 key 的设置: followee:userId:entityType -> 用户关注的实体 -> zset(entityId, nowTime)
为关注设置 key 前缀为 followee, userId:entityType 为用户关注的实体 -> 这里的实体主要指的是用户
我们使用 zset() 存取结果 传入的是 entityId 实体ID 以及 点赞那一刻的时间
这样做的目的到时候可以有助于我们排序

实现用户拥有的粉丝
key 的设置: follower:entityType:entityId -> 实体对应的粉丝 -> zset(userId, nowTime)
key 前缀为: follower, entityType:entityId 为实体对应的粉丝 -> 粉丝当然为用户对应的是 userId

实现关注与取消关注对应的操作主要有: 关注数增减、粉丝数的增减、是否关注(button对应的状态)
```

```
前端需要注意的事项
获取 entityId : <input type="hidden" id="entityId" th:value="${user.id}">
在 js 中获取: $(btn).prev().val()
还需要注意的点是就是关注button的状态切换
th:class="|btn ${hasFollowed?'btn-secondary':'btn-info'} btn-sm float-right mr-5 follow-btn|"
当用户为自己的时候不能现实关注 button
th:if="${loginUser != null && loginUser.id != user.id}"
```



> 关注列表、粉丝列表的实现

```
实现关注列表
获取 followee 的 key : followee:userId:entityType 
userId: 用户的 id 这个可以很好获取
entityType: 实体类型, 这里默认为用户设置为 3
首先通过 followeeKey 获取所有关注的目标id: targetIds
通过遍历 targetIds 然后查询这个 targetId 对应的用户属性 user 然后存入到 map.put("user", user)
其次我们还需要获取的是当前用户关注某个实体的时间,可以通过:redisTemplate.opsForZSet().score(followeeKey, targetId)
然后存入到 map.put("followedTime", score)
```

```
实现粉丝列表
获取 follower 的 key : follower:entityPe:entityId
entityType: 实体类型, 默认为用户设置为3
entityId: 实体 Id, 这是是其他实体对当前用户的关注, 也就是其他用户的 userId
下面的做法和 关注列表一样的 通过 folloerKey 获取所有关注的目标id: targetIds
```

```
判断是否关注
通过构造 followeeKey -> followee:userId:entityType
然后通过 redisTemplate.opsForZset().score(followeeKey, entityId) 是否为 null 来判断状态
```



> 优化登录模块

```
使用 Redis 存储验证码
原因: 验证码需要频繁的访问与刷新, 对性能要求高; 验证码不需要永久保存，通常在很短的时间内就会失效
验证码保存由 redis 取代 session 来保存
这样做是因为 验证码不需要永久保存, 通常在很短的时间内就会消失
```

```
使用 Redis 存储登录凭证
原因:处理每次请求时， 都要查询用户登录凭证，访问频率非常高
登录凭证由 redis 保存 取消将 登录凭证保存到 mysql 中
首先将原来的 LoginTicketMapper 里面的方法都给注释掉 @Deprecated
然后我们在登录的时候将 生成的登录凭证存入到 redis 中
在退出的时候 我们修改 redis 里面凭证的状态为 1
这样我们就不需要通过操作数据库来更新凭证
```

```
使用 redis 缓存用户信息
目的: 处理每次请求时， 都要根据凭证查询用户信息，访问频率非常高
实现这个操作主要分三步:
1. 优先从缓存中读取数据
2. 缓存中不存在数据,那么先从数据库中查询, 然后再将查询到的结果存入到 redis 中
3. 清除缓存, 主要针对于一些更新操作, 我们在更新操作完成之后清空缓存
```



> 完成 我的帖子、我回复的帖子功能模块

```
查看我的帖子功能逻辑如下
通过 userId 在 discuss_post 查询当前用户有多少帖子,然后统计数量,最后在分页显示
查看我回复帖子的逻辑如下:
这里我们只查询我们对帖子的评论: entity_type = 1 代表的是帖子
通过 userId 即可查询到全部评论 但是这里面包含对用户的评论 所以我们这里加上限制条件 entity_type = 1 即可
```

