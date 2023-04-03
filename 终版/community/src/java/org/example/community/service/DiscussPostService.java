package org.example.community.service;

import org.example.community.dao.DiscussPostMapper;
import org.example.community.entity.DiscussPost;
import org.example.community.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.util.HtmlUtils;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    public List<DiscussPost> findDiscussPost(int id, int offset, int limit, int orderMode){
        return discussPostMapper.selectDiscussPosts(id, offset, limit, orderMode);
    }

    public int findDiscussPostRows(int id){
        return discussPostMapper.selectDiscussPostRows(id);
    }


    public int addDiscussPost(DiscussPost discussPost){
        if(discussPost == null){
            throw new IllegalArgumentException("参数不能为空!");
        }
        // 转义HTML标记: 比如用户输入的标题和内容为<script>xxx</script> 我们存入数据库需要将这个标签给解析其他格式
        // 标题需要解析
//        discussPost.setTitle(HtmlUtils.htmlEscape(discussPost.getTitle()));
        // 内容不需要
        discussPost.setContent(HtmlUtils.htmlUnescape(discussPost.getContent()));
        // 过滤敏感词
        discussPost.setTitle(sensitiveFilter.filter(discussPost.getTitle()));

        return discussPostMapper.insertDiscussPost(discussPost);
    }


    public DiscussPost findDisPostById(int id){
        return discussPostMapper.selectDiscussPostById(id);
    }


    public int updateCommentCount(int id, int commentCount){
        return discussPostMapper.updateCommentCount(id, commentCount);
    }

    public int updateType(int id, int type){
        return discussPostMapper.updateType(id, type);
    }

    public int updateStatus(int id, int status){
        return discussPostMapper.updateStatus(id, status);
    }

    public int updateScore(int id, double score){
        return discussPostMapper.updateScore(id, score);
    }

    public int findPostCountById(int userId){
        return discussPostMapper.selectPostCountById(userId);
    }
    public List<DiscussPost> findPostById(int userId, int offset, int limit){
        return discussPostMapper.selectPostById(userId, offset, limit);
    }




}
