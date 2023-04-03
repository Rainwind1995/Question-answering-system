package org.example.community.dao;


import org.apache.ibatis.annotations.Mapper;
import org.example.community.entity.LoginTicket;

@Mapper
@Deprecated
public interface LoginTicketMapper {

    int insertLoginTicket(LoginTicket loginTicket);

    LoginTicket selectByTicket(String ticket);

    int updateStatus(String ticket, int status);

}
