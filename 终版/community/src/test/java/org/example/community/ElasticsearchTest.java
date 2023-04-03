package org.example.community;


import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightField;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.example.community.dao.DiscussPostMapper;
import org.example.community.dao.elasticsearch.DiscussPostRepository;
import org.example.community.entity.DiscussPost;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.SearchResultMapper;
import org.springframework.data.elasticsearch.core.aggregation.AggregatedPage;
import org.springframework.data.elasticsearch.core.aggregation.impl.AggregatedPageImpl;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.SearchQuery;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
@ContextConfiguration(classes = CommunityApplication.class)

public class ElasticsearchTest {

    @Autowired
    private DiscussPostMapper discussMapper;

    @Autowired
    private DiscussPostRepository discussRepository;

    @Autowired
    private ElasticsearchTemplate elasticsearchTemplate;


    @Test
    public void testInsert() {
        discussRepository.save(discussMapper.selectDiscussPostById(241));
        discussRepository.save(discussMapper.selectDiscussPostById(242));
        discussRepository.save(discussMapper.selectDiscussPostById(243));
    }

    @Test
    public void testInsertList() {
        discussRepository.saveAll(discussMapper.selectDiscussPosts(101, 0, 100, 0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(102, 0, 100, 0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(103, 0, 100, 0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(111, 0, 100, 0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(112, 0, 100, 0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(131, 0, 100, 0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(132, 0, 100, 0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(133, 0, 100, 0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(134, 0, 100, 0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(149, 0, 100, 0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(168, 0, 100, 0));
        discussRepository.saveAll(discussMapper.selectDiscussPosts(171, 0, 100, 0));
    }


    @Test
    public void testUpdate() {
        DiscussPost discussPost = discussMapper.selectDiscussPostById(231);
        discussPost.setContent("哈哈啊哈哈");
        discussRepository.save(discussPost);
    }


    @Test
    public void testDelete() {
        // 删除单条数据
        // discussRepository.deleteById(231);
        // 删除全部数据
        // discussRepository.deleteAll();
    }

    @Test
    public void testSearchByRepository() {
        // 这种方法有个弊端 就是不能通过标签高亮显示出我们需要查询的关键字
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content")) // 查询条件
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC)) // 先按照 type 逆序排序
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC)) // 上面不满足 则按照 分数逆序排序
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC)) // 上面不满足 则按照创建时间排序
                .withPageable(PageRequest.of(0, 10)) // 设置每页的显示数量
                .withHighlightFields(  // 设置需要高亮的地方
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        Page<DiscussPost> page = discussRepository.search(searchQuery);
        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());

        for (DiscussPost post : page) {
            System.out.println(post);
        }
    }

    @Test
    public void testSearchByTemplate() {
        // 1.创建查询对象 设置查询条件 执行查询
        SearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.multiMatchQuery("互联网寒冬", "title", "content")) // 查询条件
                .withSort(SortBuilders.fieldSort("type").order(SortOrder.DESC)) // 先按照 type 逆序排序
                .withSort(SortBuilders.fieldSort("score").order(SortOrder.DESC)) // 上面不满足 则按照 分数逆序排序
                .withSort(SortBuilders.fieldSort("createTime").order(SortOrder.DESC)) // 上面不满足 则按照创建时间排序
                .withPageable(PageRequest.of(0, 10)) // 设置每页的显示数量
                .withHighlightFields(  // 设置需要高亮的地方
                        new HighlightBuilder.Field("title").preTags("<em>").postTags("</em>"),
                        new HighlightBuilder.Field("content").preTags("<em>").postTags("</em>")
                ).build();

        Page<DiscussPost> page = elasticsearchTemplate.queryForPage(searchQuery, DiscussPost.class, new SearchResultMapper() {
            @Override
            public <T> AggregatedPage<T> mapResults(SearchResponse response, Class<T> aClass, Pageable pageable) {
                // 2. 获取结果集
                SearchHits hits = response.getHits();

                List<DiscussPost> list = new ArrayList<>();

                // 遍历
                for (SearchHit hit : hits) {
                    DiscussPost post = new DiscussPost();

                    String id = hit.getSourceAsMap().get("id").toString();
                    post.setId(Integer.valueOf(id));

                    String userId = hit.getSourceAsMap().get("userId").toString();
                    post.setUserId(Integer.valueOf(userId));

                    String title = hit.getSourceAsMap().get("title").toString();
                    post.setTitle(title);

                    String content = hit.getSourceAsMap().get("content").toString();
                    post.setContent(content);

                    String status = hit.getSourceAsMap().get("status").toString();
                    post.setStatus(Integer.valueOf(status));

                    String createTime = hit.getSourceAsMap().get("createTime").toString();
                    post.setCreateTime(new Date(Long.valueOf(createTime)));

                    String commentCount = hit.getSourceAsMap().get("commentCount").toString();
                    post.setCommentCount(Integer.valueOf(commentCount));

                    // 3. 处理显示高亮结果
                    HighlightField titleField = hit.getHighlightFields().get("title");
                    if (titleField != null) {
                        // 获取的结果集是一个数组 我们只需要取第一个
                        post.setTitle(titleField.getFragments()[0].toString());
                    }
                    HighlightField contentField = hit.getHighlightFields().get("content");
                    if (contentField != null) {
                        // 获取的结果集是一个数组 我们只需要取第一个
                        post.setContent(contentField.getFragments()[0].toString());
                    }

                    // 4. 添加到集合
                    list.add(post);
                }

                return new AggregatedPageImpl(list, pageable, hits.getTotalHits(), response.getAggregations(),
                        response.getScrollId(), hits.getMaxScore());
            }
        });

        System.out.println(page.getTotalElements());
        System.out.println(page.getTotalPages());
        System.out.println(page.getNumber());
        System.out.println(page.getSize());

        for (DiscussPost post : page) {
            System.out.println(post);
        }

    }

}
