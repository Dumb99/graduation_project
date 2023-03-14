package com.baiyun.asstorage.service.impl;

import com.baiyun.asstorage.dto.UserDTO;
import com.baiyun.asstorage.helper.JwtHelper;
import com.baiyun.asstorage.mapper.UserMapper;
import com.baiyun.asstorage.service.UserService;
import com.baiyun.asstorage.vo.CheckVO;
import io.netty.util.internal.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.util.*;

@Service
public class UserServiceImpl implements UserService {

    public static Logger logger = LoggerFactory.getLogger(UserServiceImpl.class);

    @Resource
    private RedisTemplate<String,Object> redisTemplate;

    @Resource
    private MongoTemplate mongoTemplate;

    @Resource
    private UserMapper userMapper;

    @Override
    public boolean send(String email, String code) {
        if(StringUtil.isNullOrEmpty(email)) return false;
        //整合邮件服务
        final ApplicationContext context = new ClassPathXmlApplicationContext(
                new String[] {"applicationContext.xml"});
        JavaMailSender sender = (JavaMailSender) context.getBean("mailSender");

        logger.info("sending message, phone:{}, code: {}",email, code);

        // SimpleMailMessage只能用来发送text格式的邮件
        try {
            SimpleMailMessage mail = new SimpleMailMessage();
            mail.setTo(email);
            mail.setFrom("hit180400304@163.com");
            mail.setSubject("【路由路径异常检测系统】");
            mail.setText("尊敬的用户：\n您本次登陆的验证码为："+code+"，2分钟内有效，为了保障您的账户安全，请勿向他人泄漏验证码信息。");
            sender.send(mail);
//            sendByAli(email,code);
            return true;
        } catch (Exception e) {
            logger.error(e.toString());
            throw new RuntimeException(e);
        }
    }

    @Override
    public Map<String, Object> login(String email, String code) {
        if (StringUtil.isNullOrEmpty(email) || StringUtil.isNullOrEmpty(code)){
            throw  new RuntimeException("非法格式输入");
        }
        Object redisCode = redisTemplate.opsForValue().get(email);
        if(!code.equals(redisCode.toString())){
            throw new RuntimeException("验证码校验失败");
        }
        UserDTO userDTO = userMapper.selectByEmail(email);
        if (Objects.isNull(userDTO)){
            UserDTO user = new UserDTO();
            user.setEmail(email);
            userMapper.insert(user);
        }
        Map<String, Object> map = new HashMap<>();
        String name = Objects.isNull(userDTO.getNickName()) ? "Bob" : userDTO.getNickName();
        map.put("name",name);
        map.put("email",email);
        map.put("token", JwtHelper.createToken(userDTO.getEmail(),name));
        return map;
    }

    @Override
    public List<CheckVO> findAll(String email){
        Query query = new Query(Criteria.where("email").is(email));
        query.with(Sort.by(Sort.Order.desc("time")));
        return mongoTemplate.find(query, CheckVO.class);
    }


    public void sendByAli(String email, String code){
        // 配置发送邮件的环境属性
        final Properties props = new Properties();

        // 表示SMTP发送邮件，需要进行身份验证
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.host", "smtp.163.com");
        //设置端口:
        props.put("mail.smtp.port", "80");//或"25", 如果使用ssl，则去掉使用80或25端口的配置，进行如下配置：
        //加密方式:
        //props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        //props.put("mail.smtp.socketFactory.port", "465");
        //props.put("mail.smtp.port", "465");

        props.put("mail.smtp.from", "hit180400304@163.com");    //mailfrom 参数
        props.put("mail.user", "hit180400304@163.com");// 发件人的账号(在控制台创建的发信地址)
        props.put("mail.password", "RGEKNYHHUFSCNAGP");// 发信地址的smtp密码(在控制台选择发信地址进行设置)

        //使用环境属性和授权信息，创建邮件会话
        Session mailSession = Session.getInstance(props);
        //mailSession.setDebug(true);//开启debug模式

        //UUID uuid = UUID.randomUUID();
        //final String messageIDValue = "<" + uuid.toString() + ">";
        //创建邮件消息
        MimeMessage message = new MimeMessage(mailSession) {
            //@Override
            //protected void updateMessageID() throws MessagingException {
            //设置自定义Message-ID值
            //setHeader("Message-ID", messageIDValue);//创建Message-ID
            //}
        };

        try {
            // 设置发件人邮件地址和名称。填写控制台配置的发信地址。和上面的mail.user保持一致。名称用户可以自定义填写。
            InternetAddress from = new InternetAddress("hit180400304@163.com", "路由路径异常检测系统");//from 参数,可实现代发，注意：代发容易被收信方拒信或进入垃圾箱。
            message.setFrom(from);


            // 设置收件人邮件地址
            InternetAddress to = new InternetAddress(email);
            message.setRecipient(MimeMessage.RecipientType.TO, to);
            //如果同时发给多人，才将上面两行替换为如下（因为部分收信系统的一些限制，尽量每次投递给一个人；同时我们限制单次允许发送的人数是60人）：
            //InternetAddress[] adds = new InternetAddress[2];
            //adds[0] = new InternetAddress("收信地址");
            //adds[1] = new InternetAddress("收信地址");
            //message.setRecipients(Message.RecipientType.TO, adds);

            message.setSentDate(new Date()); //设置时间

            //设置邮件标题
            message.setSubject("【路由路径异常检测系统】");
            message.setContent("尊敬的用户：\n您本次登陆的验证码为："+code+"，2分钟内有效，为了保障您的账户安全，请勿向他人泄漏验证码信息。", "text/html;charset=UTF-8");//html超文本；// "text/plain;charset=UTF-8" //纯文本。

//            //若需要开启邮件跟踪服务，请使用以下代码设置跟踪链接头。前置条件和约束见文档"如何开启数据跟踪功能？"
//            String tagName = "Test";
//            HashMap<String, String> trace = new HashMap<>();
//            //这里为字符串"1"
//            trace.put("OpenTrace", "1");      //打开邮件跟踪
//            trace.put("LinkTrace", "1");     //点击邮件里的URL跟踪
//            trace.put("TagName", tagName);   //控制台创建的标签tagname
//            String jsonTrace = new GsonBuilder().setPrettyPrinting().create().toJson(trace);
//            //System.out.println(jsonTrace);
//            String base64Trace = new String(Base64.getEncoder().encode(jsonTrace.getBytes()));
//            //设置跟踪链接头
//            message.addHeader("X-AliDM-Trace", base64Trace);
//            //邮件eml原文中的示例值：X-AliDM-Trace: eyJUYWdOYW1lIjoiVGVzdCIsIk9wZW5UcmFjZSI6IjEiLCJMaW5rVHJhY2UiOiIxIn0=

//发送附件和内容：
//            BodyPart messageBodyPart = new MimeBodyPart();
//            //messageBodyPart.setText("消息<br>Text");//设置邮件的内容，文本
//            messageBodyPart.setContent("测试<br> 内容", "text/html;charset=UTF-8");// 纯文本："text/plain;charset=UTF-8" //设置邮件的内容
//            //创建多重消息
//            Multipart multipart = new MimeMultipart();
//            //设置文本消息部分
//            multipart.addBodyPart(messageBodyPart);

//            //附件部分
//            //发送附件，总的邮件大小不超过15M，创建消息部分。
//            MimeBodyPart mimeBodyPart = new MimeBodyPart();
//            //设置要发送附件的文件路径
//            String filename = "C:\\Users\\test2.txt";
//            FileDataSource source = new FileDataSource(filename);
//            mimeBodyPart.setDataHandler(new DataHandler(source));
//            //处理附件名称中文（附带文件路径）乱码问题
//            mimeBodyPart.setFileName(MimeUtility.encodeText("测试附件"));
//            mimeBodyPart.addHeader("Content-Transfer-Encoding", "base64");
//            multipart.addBodyPart(mimeBodyPart);
//            //发送含有附件的完整消息
//            message.setContent(multipart);
//            // 发送附件代码，结束

            // 发送邮件
            Transport.send(message);

        } catch (MessagingException e) {
            String err = e.getMessage();
            // 在这里处理message内容， 格式是固定的
            System.out.println(err);
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
