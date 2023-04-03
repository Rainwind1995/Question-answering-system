> #### Elasticsearch 安装

```
官网: https://www.elastic.co/cn/
下载链接: https://www.elastic.co/cn/downloads/past-releases#elasticsearch (选择版本 6.4.3)
修改解压后 config 文件下的 elasticsearch.yml 
cluster.name: nowcode
path.data: d:\data\elasticsearch_data
path.logs: d:\data\elasticsearch_log
配置环境变量
D:\elasticsearch-6.4.3\bin
```



> #### Elasticsearch ik 安装

```
官网: https://github.com/medcl/elasticsearch-analysis-ik
下载和 elasticsearch 版本一致的 elasticsearch ik
https://github.com/medcl/elasticsearch-analysis-ik/releases/tag/v6.4.3
解压 elasticsearch ik 到 elasticsearch 目录下的 plugins 中这里需要新建 ik 文件夹
```



> #### PostMan 安装

```
下载地址: https://www.postman.com/
```



> #### 测试 Elasticsearch

```
查看健康状态
curl -X GET "localhost:9200/_cat/health?v"
查看节点
curl -X GET "localhost:9200/_cat/nodes?v"
查看索引
curl -X GET "localhost:9200/_cat/indices?v"
新建索引
curl -X PUT "localhost:9200/test"
删除索引
curl -X DELETE "localhost:9200/test"
```



> #### SpringBoot 整合 Elasticsearch

```
导入依赖, 选择 2.1.6
https://mvnrepository.com/artifact/org.springframework.boot/spring-boot-starter-data-elasticsearch
配置 ElasticserachProperties:
spring.data.elasticsearch.cluster-name=nowcoder
spring.data.elasticsearch.cluster-nodes=127.0.0.1:9300
```



> #### 相关注意事项

```
启动 elasticsearch 失败解决方法
原因是 netty 启动冲突问题, 需要在 CommunityApplication 编写 init()
System.setProperty("es.set.netty.runtime.available.processors", "false");
```

```
elasicsearch 版本冲突解决方案
按照课程流程走的话 elasticsearch 版本和 elasticsearch ik 都是 6.4.3
其次就是Springboot 版本必须是 2.1.5, 高版本的话建议将 Springboot 降低到低版本
如果上面版本太高或者太低会出现各种不兼容的问题
```



> #### 开发社区搜索功能

```
搜索服务
将帖子保存到 Elasticsearch 服务器
从 Elasticserach 服务器删除帖子
从 Elasticsearch 服务器搜索帖子
```

```
发布事件
发布帖子时, 将贴子异步的提交到 Elasticsearch 服务器上
点击发布帖子, 然后再发布帖子那里设置一个发布帖子事件, 当我们点击发布的时候就将这一条帖子内容添加到 Elasticsearch 服务器上
增加评论时, 将帖子异步的提交到 Elasticsearch 服务器上
同理, 对帖子进行评论的时候也会触发相应的事件,只不过我们评论的对象要是帖子才会触发
在消费事件中增加一个方法, 消费帖子发布事件
增加的是一个 发布帖子的主题然后再里面调用 save() 将帖子添加到 Elasticsearch 服务器上
```

```
显示结果
结果的显示通过后台传值给前端然后渲染
```

```
解决使用Elasicsearch 无法搜索英文
参考文章: https://www.jianshu.com/p/5714115e8a90
解决方案:
官网下载 pinyin 插件, 安装到 plugins 目录下面
下载地址: https://github.com/medcl/elasticsearch-analysis-pinyin/releases (里面选择合适的版本)
```

