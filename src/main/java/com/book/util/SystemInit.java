package com.book.util;

import com.book.bean.BookType;
import com.book.bean.FatherType;
import com.book.redis.RedisUtils;
import com.book.service.BookService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.util.List;

/**
 * Created by Myth on 2017/2/15 0015
 */
public class SystemInit implements ServletContextListener {

//    @Autowired
    RedisUtils utils;
//    @Autowired
    BookService bookService;
    private ApplicationContext context;

    private static Logger logger = LoggerFactory.getLogger(SystemInit.class);


    @Override
    public void contextInitialized(ServletContextEvent event) {
        logger.info("初始化系统");
        context = WebApplicationContextUtils.getWebApplicationContext(event.getServletContext());

//        ApplicationContext context = new ClassPathXmlApplicationContext("application.xml");
//        RedisUtils utils = (RedisUtils) context.getBean("redisUtils");
//        Jedis jedis = utils.getConnect();
        JedisPoolConfig config = (JedisPoolConfig)context.getBean("poolConfig");
        JedisPool pool = (JedisPool)context.getBean("jedisPool");
        Jedis jedis  = pool.getResource();
        initTypes(jedis);

//        System.out.println("缓存结果 : "+cache.get("u"));
    }

//    初始化类别的方法
    public void initTypes(Jedis jedis){
        jedis.del("BookFatherType");
        bookService = (BookService) context.getBean("bookService");
        List<FatherType> list = bookService.getAllTypes();
        int FatherSize = list.size();
        for(int i=FatherSize-1;i>=0;i--){
            FatherType fatherType = list.get(i);
            // 放入父类
            jedis.lpush("BookFatherType",fatherType.getType_name());
            List<BookType> types = fatherType.getBookTypes();
            int TypeSize = types.size();
            jedis.del(fatherType.getType_name());
            for(int j=TypeSize-1;j>=0;j--){
                jedis.lpush(fatherType.getType_name(),types.get(j).getType_name());
            }
        }
        logger.info("初始化类型成功");
    }
    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {
        logger.info("系统销毁");
    }

    public RedisUtils getUtils() {
        return utils;
    }

    public void setUtils(RedisUtils utils) {
        this.utils = utils;
    }

    public BookService getBookService() {
        return bookService;
    }

    public void setBookService(BookService bookService) {
        this.bookService = bookService;
    }
}
