package org.example.community.service;

import org.example.community.dao.DiscussPostMapper;
import org.example.community.dao.UserMapper;
import org.example.community.entity.DiscussPost;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DiscussPostService {
    @Autowired
    private DiscussPostMapper discussPostMapper;

    public List<DiscussPost> findDiscussPost(int id, int offset, int limit){
        return discussPostMapper.selectDiscussPosts(id, offset, limit);
    }

    public int findDiscussPostRows(int id){
        return discussPostMapper.selectDiscussPostRows(id);
    }




}
