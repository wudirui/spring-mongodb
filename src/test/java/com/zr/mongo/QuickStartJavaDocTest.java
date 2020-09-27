package com.zr.mongo;


import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.ClientSession;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.result.DeleteResult;
import com.mongodb.client.result.UpdateResult;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.junit.Before;
import org.junit.Test;

import java.math.BigDecimal;
import java.util.*;
import java.util.function.Consumer;

import static com.mongodb.client.model.Updates.*;
import static com.mongodb.client.model.Filters.*;


public class QuickStartJavaDocTest {
    //数据库
    private MongoDatabase db;
    //文档集合
    private MongoCollection<Document> doc;
    //连接客户端
    private MongoClient client;

    @Before
    public void init() {
        MongoClientOptions clientOptions = new MongoClientOptions.Builder()
                .maxConnectionIdleTime(6000).build();
        client = new MongoClient(new ServerAddress("192.168.0.102", 27017), clientOptions);

//        client = new MongoClient("192.168.0.102", 27017);
        db = client.getDatabase("zhangrui");
        doc = db.getCollection("users");
    }

    @Test
    public void insert() {
        Document document1 = new Document();
        document1.append("userName", "zhangrui1");
        document1.append("country", "China");
        document1.append("age", "18");
        document1.append("length", 1.77f);
//        document1.append("salary", new BigDecimal("6565.22"));

        Map<String, String> address1 = new HashMap<String, String>();
        address1.put("aCode", "0000");
        address1.put("add", "xxx000");
        document1.append("address", address1);

        //添加“favorites”子文档，其中两个属性是数组
        Map<String, Object> favorites1 = new HashMap<String, Object>();
        favorites1.put("movies", Arrays.asList("aa", "bb"));
        favorites1.put("cites", Arrays.asList("东莞", "东京"));
        document1.append("favorites", favorites1);

        Document doc2 = new Document();
        doc2.append("username", "Chen");
        doc2.append("country", "China");
        doc2.append("age", 30);
        doc2.append("lenght", 1.77f);
//        doc2.append("salary", new BigDecimal("8888.22"));
        Map<String, String> address2 = new HashMap<>();
        address2.put("aCode", "411000");
        address2.put("add", "我的地址 2");
        doc2.append("address", address2);
        Map<String, Object> favorites2 = new HashMap<>();
        favorites2.put("movies", Arrays.asList("东游记", "一路向东"));
        favorites2.put("cites", Arrays.asList("珠海", "东京"));
        doc2.append("favorites", favorites2);
        //使用 insertMany 插入多条数据
        doc.insertMany(Arrays.asList(document1, doc2));
    }

    @Test
    public void testFind() {
        final List<Document> ret = new ArrayList<>();
        //block接口专门用于处理查询出来的数据
        Consumer<Document> printDocument = new Consumer<Document>() {
            @Override
            public void accept(Document document) {
                System.out.println(document);
                ret.add(document);
            }
        };
        //select * from users  where favorites.cites has "东莞"、"东京"
        //db.users.find({ "favorites.cites" : { "$all" : [ "东莞" , "东京"]}})
        Bson all = all("favorites.cites", Arrays.asList("东莞", "东京"));//定义数据过滤器，喜欢的城市中要包含"东莞"、"东京"
        FindIterable<Document> find = doc.find(all);

        find.forEach(printDocument);


        System.out.println("------------------>" + String.valueOf(ret.size()));
        ret.removeAll(ret);


        //select * from users  where username like '%s%' and (contry= English or contry = USA)
        // db.users.find({ "$and" : [ { "username" : { "$regex" : ".*c.*"}} , { "$or" : [ { "country" : "English"} , { "country" : "USA"}]}]})

        String regexStr = ".*c.*";
        Bson regex = regex("username", regexStr);//定义数据过滤器，username like '%s%'
        Bson or = or(eq("country", "English"), eq("country", "USA"));//定义数据过滤器，(contry= English or contry = USA)
        Bson and = and(regex, or);
        FindIterable<Document> find2 = doc.find(and);
        find2.forEach(printDocument);
        System.out.println("------------------>" + String.valueOf(ret.size()));

    }

    @Test
    public void testUpdate() {
        //update  users  set age=6 where username = 'lison'
//    	db.users.updateMany({ "username" : "lison"},{ "$set" : { "age" : 6}},true)

        Bson eq = eq("username", "cang");//定义数据过滤器，username = 'cang'
        Bson set = set("age", 8);//更新的字段.来自于Updates包的静态导入
        UpdateResult updateMany = doc.updateMany(eq, set);
        System.out.println("------------------>" + String.valueOf(updateMany.getModifiedCount()));//打印受影响的行数

        //update users  set favorites.movies add "小电影2 ", "小电影3" where favorites.cites  has "东莞"
        //db.users.updateMany({ "favorites.cites" : "东莞"}, { "$addToSet" : { "favorites.movies" : { "$each" : [ "小电影2 " , "小电影3"]}}},true)

        Bson eq2 = eq("favorites.cites", "东莞");//定义数据过滤器，favorites.cites  has "东莞"
        Bson addEachToSet = addEachToSet("favorites.movies", Arrays.asList("小电影2 ", "小电影3"));//更新的字段.来自于Updates包的静态导入
        UpdateResult updateMany2 = doc.updateMany(eq2, addEachToSet);
        System.out.println("------------------>" + String.valueOf(updateMany2.getModifiedCount()));
    }

    @Test
    public void testDelete() {

        //delete from users where username = ‘cang’
        //db.users.deleteMany({ "username" : "cang"} )
        Bson eq = eq("username", "cang");//定义数据过滤器，username='cang'
        DeleteResult deleteMany = doc.deleteMany(eq);
        System.out.println("------------------>" + String.valueOf(deleteMany.getDeletedCount()));//打印受影响的行数

        //delete from users where age >8 and age <25
        //db.users.deleteMany({"$and" : [ {"age" : {"$gt": 8}} , {"age" : {"$lt" : 25}}]})

        Bson gt = gt("age", 8);//定义数据过滤器，age > 8，所有过滤器的定义来自于Filter这个包的静态方法，需要频繁使用所以静态导入
//    	Bson gt = Filter.gt("age",8);

        Bson lt = lt("age", 25);//定义数据过滤器，age < 25
        Bson and = and(gt, lt);//定义数据过滤器，将条件用and拼接
        DeleteResult deleteMany2 = doc.deleteMany(and);
        System.out.println("------------------>" + String.valueOf(deleteMany2.getDeletedCount()));//打印受影响的行数
    }

    @Test
    public void testTransaction() {
//		begin
//		update  users  set lenght= lenght-1  where username = ‘james’
//		update  users  set lenght= lenght+1  where username = ‘lison’
//		commit
        ClientSession clientSession = client.startSession();
        clientSession.startTransaction();
        Bson eq = eq("username", "james");
        Bson inc = inc("lenght", -1);
        doc.updateOne(clientSession,eq,inc);

        Bson eq2 = eq("username", "lison");
        Bson inc2 = inc("lenght", 1);

        doc.updateOne(clientSession,eq2,inc2);
        clientSession.commitTransaction();
        //clientSession.abortTransaction();

    }
}
