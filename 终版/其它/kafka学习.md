> 安装kafka

```
下载安装: https://kafka.apache.org/downloads
自定义修改 zookeeper.properties
dataDir=d:/data/kafka_data/zookeeper
自定义修改 server.properties
log.dirs=d:/data/kafka_data/kafka-logs
```



> 使用kafka

```
首先需要启动 zookeeper 
进入 zookeeper 的安装目录: 
d:
cd d:\kafka_2.13-3.2.0
bin\windows\zookeeper-server-start.bat config\zookeeper.properties
启动完成之后不要关闭
```

```
首先需要启动  kafka
进入 kafka 的安装目录: 
d:
cd d:\kafka_2.13-3.2.0
bin\windows\kafka-server-start.bat config\server.properties
启动完成之后不要关闭
```

```
使用 kafka 生产消息
d:
cd d:\kafka_2.13-3.20\bin\windows
创建主题: kafka-topics.bat --create --bootstrap-server localhost:9092 --replication-factor 1 --partitions 1 --topic test
查看创建的主题: kafka --topics.bat --list --bootstrap-server localhost:9092
生产消息: kafka-console-producter.bat --broker-list localhost:9092 --topic test
```

```
使用 kafka 接收消息
d:
cd d:\kafka_2.13-3.20\bin\windows
接收消息: kafka-console-consumer.bat --bootstrap-server localhost:9092 --topic test --from-beginning
```



> SpringBoot 整合 kafka

```
引入依赖: https://mvnrepository.com/artifact/org.springframework.kafka/spring-kafka
配置 appication.properties 里面配置 kafkaProperties 
编写一个测试类测试 Kafka
出现一个小 Bug 就是程序正常运行 但是无法输出消息, 解决方案如下所示: 
在 consumer.properties 配置文件里面添加下面这句话:
listeners=PLAINTEXT://localhost:9092   
```



> 发送系统通知

```
系统发布通知主要针对下面的触发事件:
评论之后,发布通知
点赞之后,发布通知
关注之后,发布通知
针对触发的事件我们需要处理事件:
封装事件对象
开发事件的生产者
开发事件的消费者
```

```
功能实现流程
发送系统通知主要还是针对的是 message 这个数据表来进行操作的, 只不过我们采用了 kafka 中间件在触发事件之后目标对象能立刻接收到系统通知。
因此, 我们需要围绕着 message 里面的字段来构建 Event 对象, Event 对象字段主要有如下:
topic: 发送的主题 (comment-评论、like-点赞、follow-关注)
userId: 发送消息的用户 id 这里默认是系统所以为 1
entityType: 实体类型 (帖子-1、评论-2、用户-3)
entityId: 实体类型 id
entityUserId:实体用户 id (其实这里就是指的是接收系统通知的用户 id => to_id)
Map<String, Object> data = new HashMap<>() : 这个哈希表用来存储额外的字段的,不如帖子 id => postId
相反, 我们需要对 Event 对象里的 set 方法返回类型修改为 Event ,这样做的好处就是为了简化代码书写不需要每次 event.set()
```

```
构建生产者和消费者
生产者主要是发送消息: 传给生产者的主要就是 topic 和 content (这个是主动的)
消费者主要是接收消息: 接收生产者发送过来的消息然后解析成 message 字段里面的内容 最后将通知添加到 message 数据表里面
```

```
触发事件的实现
评论触发事件: 我们只需要在 CommentController 里找到实现评论那个方法在最下面添加触发事件即可
同理,点赞和关注触发事件也是同样的操作
```

```
注意事项:
测试系统发送通知的过程中需要将 zookeeper 和 kafka 打开
验证自己的逻辑是否正确只要观察我们触发这三个事件之后数据库中 message 是否包含那三条数据即可
```



> 显示系统通知

```
通知列表
显示评论、点赞、关注三种类型的通知
通知详情
分页显示某一类所包含的通知
未读消息
在 index 显示所有未读消息的数量
```

```
功能描述
实现显示系统通知主要是将评论、点赞、关注这三类通知显示出来，这里我们默认显示最近的一条通知,并不是将每种类型的通知全部展示出来,只有我们点击每种类型的通知的时候跳转到相应界面然后将这种类型的所有消息展示,如果是评论或者点赞我们可以跳转到点赞或者评论的原地址,相反,如果是关注的话我们点击是展示的他们的个人主页。
```

```
实现通知列表逻辑
编写 SQL 语句我们只需要查询最近的一条消息,最近的一条消息可以通过 id 来获取,因为后加入的消息 id 是比上一次的都大
select <include refid="selectFields"></include>
from message
where id in(
	select max(id) from message 
	where status != 2
	and from_id = 1
	and to_id = #{userId}
	and conversationId = #{topic}
)
通过上面这条语句我们就完成了获取对三种类型最近的一条消息获取。
在 MessageController 里面编写函数 通过获取这三种类型通知的一条 message 我么即可拿到里面的所有数据,最后展示在前端页面
```

```
实现通知详情
编写 SQL 语句然后我们根据传入的通知类型来获取每种通知的所有详情
select <include refid="selectFields"></include>
from message
where status != 2
and from_id = 1
and to_id = #{userId}
and conversationId = #{topic}
order by create_time desc
limit #{offset}, #{limit}
通过上面这条 SQL 语句我们传入不同的 topic 就可查询每种通知类型的全部消息。
```

```
未读消息
实现逻辑主要就是将 私信未读消息 和 通知未读消息总和加在一起。
这里主要讲解下 通知未读消息的实现
编写 SQL 语句如下所示: 
select count(id) from message 
where status != 2
and from_id = 1
and to_id = #{userId}
<if test="topic != null">
	and conversationId = #{topic}
</if>
这里加了一个 if 主要的作用就是如果 topic 为空那么我们查询的是三种通知类型所有未读的数量
如果 topic 不为空 那么我们查询的就是单个通知类型的未读数量
```

