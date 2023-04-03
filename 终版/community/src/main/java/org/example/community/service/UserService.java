package org.example.community.service;

import org.apache.commons.lang3.StringUtils;
import org.example.community.dao.LoginTicketMapper;
import org.example.community.dao.UserMapper;
import org.example.community.entity.LoginTicket;
import org.example.community.entity.User;
import org.example.community.util.CommunityConstant;
import org.example.community.util.CommunityUtil;
import org.example.community.util.MailClient;
import org.example.community.util.RedisKeyUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
public class UserService implements CommunityConstant {

    @Autowired
    private UserMapper userMapper;

//    @Autowired
//    private LoginTicketMapper loginTicketMapper;

    @Autowired
    private MailClient mailClient;

    @Autowired
    private TemplateEngine templateEngine; // 导入模板引擎

    @Value("${community.path.domain}")
    private String domain; // 自定义的域名

    @Value("${server.servlet.context-path}")
    private String contextPath; // 项目路径

    @Autowired
    private RedisTemplate redisTemplate;

    // 通过id查找用户
    public User findUserById(int id){
        //  return userMapper.selectById(id);
        // 这里通过 redis 查找
        User user = getCache(id);
        if(user == null){
            // 如果缓存中不存在则从数据库中查询
            user = initCache(id);
        }
        return user;
    }

    // 查询所有用户
    public int findAllUserRows(){
        return userMapper.selectUserRows();
    }

    // 分页查询所有用户
    public List<User> findAllUser(int offset, int limit){
        return userMapper.selectAllUser(offset, limit);
    }


    // 注册
    // 这里返回类型为什么是map,因为我们在注册的时候需要去判断username、password、email这些字段是否为空,所以需要有提示信息
    // 可以map通过保存这些提示信息
    public Map<String, Object> register(User user){
        Map<String, Object> map = new HashMap<>();

        // 空值处理:传入的user不能是一个空值
        if(user == null){
            throw new IllegalArgumentException("参数不能为空");
        }
        // 判断用户名是否为空
        if(StringUtils.isBlank(user.getUsername())){
            map.put("usernameMsg", "账户名不能为空");
            return map;
        }
        // 判断密码是否为空
        if(StringUtils.isBlank(user.getPassword())){
            map.put("passwordMsg", "密码不能为空");
            return map;
        }

        // 后期可以把这个加上去,目前主要测试方便不进行密码长度校验
//        if(user.getPassword().length() < 8){
//            map.put("passwordMsg", "密码不能低于8位");
//            return map;
//        }
        // 判断邮箱是否为空
        if(StringUtils.isBlank(user.getEmail())){
            map.put("emailMsg", "邮箱不能为空");
            return map;
        }

        // 注册之前需要判断当前用户是否已经存在
        User u = userMapper.selectByName(user.getUsername());
        // 不为null说明该用户已经存在
        if(u != null){
            map.put("usernameMsg", "用户名已存在");
            return map;
        }

        // 验证邮箱是否存在
        if(userMapper.selectByEmail(user.getEmail()) != null){
            map.put("emailMsg", "邮箱已被注册");
            return map;
        }

        // 注册用户
        // 设置用户名
        user.setUsername(user.getUsername());
        // 生成5位随机字符串
        user.setSalt(CommunityUtil.generateUUID().substring(0 , 5));
        // 使用 md5 加密密码
        user.setPassword(CommunityUtil.md5(user.getPassword()) + user.getSalt());
        // 设置用户类型
        user.setType(0);
        // 设置用户状态（0表示未激活）
        user.setStatus(0);
        // 设置激活码
        user.setActivationCode(CommunityUtil.generateUUID());
        // 设置头像路径(这里使用牛客网上的默认头像,牛客网上有1001个头像所以我们随机生成其中的一个)
        user.setHeaderUrl(String.format("http://images.nowcoder.com/head/%dt.png", new Random().nextInt(1000)));
        // 设置注册时间
        user.setCreateTime(new Date());
        userMapper.insertUser(user);


        // 发送邮箱
        // 运行上下文:用来保存模型数据，当模板引擎渲染时，可以从Context上下文中获取数据用于渲染。
        // 当与SpringBoot结合使用时，我们放入Model的数据就会被处理到Context，作为模板渲染的数据使用
        Context context = new Context();
        // 设置上下文参数
        context.setVariable("email", user.getEmail());
        // 设置邮件激活路径比如: http://localhost:8080/community/activation/101/code
        String url = domain + contextPath + "/activation/" + user.getId() + "/" + user.getActivationCode();
        context.setVariable("url", url);
        String content = templateEngine.process("/mail/activation", context);
        // 发送邮箱
        mailClient.sendMail(user.getEmail(), "激活邮箱", content);

        return map;
    }

    // 激活邮箱
    public int activation(int userId, String code){
        // 通过userId获取user对象
        User user = userMapper.selectById(userId);
        if(user.getStatus() == 1){ // 表示重复激活了
            return ACTIVATION_REPEAT;
        }else if(user.getActivationCode().equals(code)){ // 表示还未激活
            // 更新状态
            userMapper.updateStatus(userId, 1);
            // 状态更新之后 直接清空缓存
            clearCache(userId);
            return ACTIVATION_SUCCESS;
        }else{ // 激活失败
            return ACTIVATION_FAILURE;
        }
    }


    // 登录
    public Map<String, Object> login(String username, String password, int expiredSeconds){
        Map<String, Object> map = new HashMap<>();

        // 检验用户名是否为空
        if(StringUtils.isBlank(username)){
            map.put("usernameMsg", "用户名不能为空");
            return map;
        }

        // 检验密码是否为空
        if(StringUtils.isBlank(password)){
            map.put("passwordMsg", "密码不能为空");
            return map;
        }

        // 校验当前用户是否存在
        User user = userMapper.selectByName(username);

        if(user == null){
            map.put("usernameMsg", "当前账号不存在");
            return map;
        }

        // 判断用户激活状态
        if(user.getStatus() == 0){
            map.put("usernameMsg", "该用户未被激活");
            return map;
        }

        // 校验密码是否一致
        // 我们在前端输入的密码是明文没有经过加密的,而数据库存放的是加密后的密码,所以我们需要对输入的密码进行md5加密然后加上salt
        // 得到的加密密码在与数据库里面的进行比较
        password = CommunityUtil.md5(password) + user.getSalt();
        if(!user.getPassword().equals(password)){
            map.put("passwordMsg", "密码输入错误");
            return map;
        }

        // 上述都不满足, 生成登陆凭证
        LoginTicket loginTicket = new LoginTicket();
        loginTicket.setUserId(user.getId());
        loginTicket.setTicket(CommunityUtil.generateUUID());
        loginTicket.setStatus(0);
        loginTicket.setExpired(new Date(System.currentTimeMillis() + expiredSeconds * 1000));
        //   loginTicketMapper.insertLoginTicket(loginTicket);

        // 获取 ticket key
        String redisKey = RedisKeyUtil.getTicketKey(loginTicket.getTicket());
        // 将 凭证存入到 redis
        redisTemplate.opsForValue().set(redisKey, loginTicket);

        // 保存凭证
        map.put("ticket", loginTicket.getTicket());

        return map;
    }

    // 退出
    public void logout(String ticket){
        // 退出直接将凭证状态设置为1
        // loginTicketMapper.updateStatus(ticket, 1);
        String redisKey = RedisKeyUtil.getTicketKey(ticket);
        // 从 redis 中获取凭证
        LoginTicket loginTicket = (LoginTicket) redisTemplate.opsForValue().get(redisKey);
        // 将凭证状态设置为1
        loginTicket.setStatus(1);
        // 在将当前状态存入到 redis
        redisTemplate.opsForValue().set(redisKey, loginTicket);
    }

    // 查询凭证
    public LoginTicket findTicket(String ticket){
        String redisKey = RedisKeyUtil.getTicketKey(ticket);

        //  return loginTicketMapper.selectByTicket(ticket);
        return (LoginTicket) redisTemplate.opsForValue().get(redisKey);
    }

    // 更新头像
    public int updateHeader(int id, String headerUrl){
        // 先更新头像
        int row = userMapper.updateHeader(id, headerUrl);
        // 更新成功才会执行清空缓存
        clearCache(id);

        return row;
    }

    // 更新密码
    public int updatePassword(int id, String password){
        return userMapper.updatePassword(id, password);
    }

    // 通过邮箱查询用户
    public User findUserByEmail(String email){
        return userMapper.selectByEmail(email);
    }

    // 通过用户名查找用户
    public User findUserByName(String username){
        return userMapper.selectByName(username);
    }


    // 1. 优先从缓存中取值
    public User getCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        return (User) redisTemplate.opsForValue().get(redisKey);
    }

    // 2. 取不到数据时,初始化缓存数据
    public User initCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        // 从数据库中获取数据
        User user = userMapper.selectById(userId);
        // 添加到 redis 中, 设置有效时间为 1h
        redisTemplate.opsForValue().set(redisKey, user, 3600, TimeUnit.SECONDS);

        return user;
    }

    // 3. 数据变更时, 清楚缓存数据
    public void clearCache(int userId){
        String redisKey = RedisKeyUtil.getUserKey(userId);
        // 直接将 key 移除即可
        redisTemplate.delete(redisKey);
    }


    public Collection<? extends GrantedAuthority> getAuthorities(int userId){
        User user = this.findUserById(userId);
        List<GrantedAuthority> list = new ArrayList<>();
        list.add(new GrantedAuthority() {
            @Override
            public String getAuthority() {
                switch (user.getType()){
                    case 1:
                        return AUTHORITY_ADMIN;
                    case 2:
                        return AUTHORITY_MODERATOR;
                    default:
                        return AUTHORITY_USER;
                }
            }
        });

        return list;
    }

}
