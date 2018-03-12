import com.google.common.collect.Lists;
import com.sun.xml.internal.ws.addressing.model.MissingAddressingHeaderException;

import java.util.*;

/**
 * @author yutong song
 * @date 2018/3/4
 */
public class Main {

    public static void main(String[] args) throws  Exception {
        //1.处理数据并将属性放入实体列表中
        List<RelationObject> relationObjectListBase = MovieDataHandler.handle(MovieDataHandler.class.getResource("/").getPath() + "u1.base");
        //2,读取userId和moveId.
        List<RelationObject> relationObjectListTest = MovieDataHandler.handle(MovieDataHandler.class.getResource("/").getPath() + "u1.test");
        //清洗数据集
        DataCleaning(relationObjectListBase,relationObjectListTest);
        int successNumber = 0;
        double MAE = 0.0;  //平均绝对误差
        double RMSE = 0.0;  //均方根误差
        for (RelationObject relationObjectTest : relationObjectListTest) {
            double rate = 0;
            double score;
            double weightSum = 0;
            //对于itemId查询看过这部电影的人
            List<RelationObject> userIdList = listUserByItemId(relationObjectListBase, relationObjectTest.getItemId());
            //3,获取和每一个看过这部电影用户的兴趣相似度
            for (RelationObject relationObject : userIdList) {
                double weight = CalUserSimilarity(relationObjectTest.getUserId(), relationObject.getUserId(), relationObjectListBase);
                System.out.println("weight:" + weight);
                //如果之前没有相同的电影则跳过
                if (weight == -1) {
                    continue;
                }
                //4,加权预测该用户对这部电影的评价
                rate = rate + weight * relationObject.getRate();
                System.out.println("rate:" + rate);
                weightSum = weightSum + weight;
                System.out.println("weightSum:" + weightSum);
            }
            score = rate / weightSum;
            System.out.println("score:" + score);
            MAE += Math.abs(relationObjectTest.getRate() - score);
            RMSE += Math.pow(relationObjectTest.getRate() - score, 2);
            if (Math.round(score) == relationObjectTest.getRate()) {
                successNumber++;
            }
            MAE = MAE / relationObjectListTest.size();
            RMSE = Math.sqrt(RMSE / relationObjectListTest.size());
        }
            System.out.println("成功率为:" + successNumber * 1.0 / relationObjectListTest.size());
            System.out.println("平均绝对误差为;" + MAE);
            System.out.println(("均方根误差为:") + RMSE);
    }
        //7,计算userId对于itemId的喜欢.
    private static List<RelationObject> listUserByItemId(List<RelationObject> relationObjectBase,int itemId)
    {
        List<RelationObject> userIdList= Lists.newArrayList();
        for(RelationObject relationObject:relationObjectBase)
        {
            if(relationObject.getItemId()==itemId)
                userIdList.add(relationObject);
        }
        return userIdList;
    }

    /**
     * 使用余弦相似度计算用户的兴趣相似度
     */
    private static double CalUserSimilarity(int UserIdA,int UserIdB,List<RelationObject> relationObjectBase)
    {
        List<Integer> MovieAList=Lists.newArrayList();
        List<Integer> MovieBList=Lists.newArrayList();
            double numerator = 0.0;
            double denomiator = 0.0;
            //A,B评过分的电影
            for (RelationObject relationObject : relationObjectBase) {
                if (relationObject.getUserId() == UserIdA)
                    MovieAList.add(relationObject.getItemId());
                if (relationObject.getUserId() == UserIdB)
                    MovieBList.add(relationObject.getItemId());
            }
            //A,B之前没有对电影评过分
            if(MovieAList.size()==0||MovieBList.size()==0)
                return -1;
            else {
                denomiator = Math.sqrt(MovieAList.size() * MovieBList.size());
                MovieAList.retainAll(MovieBList);
                //A,B之间没有看过共同的电影
                if(MovieAList.size()==0)
                    return -1;
                else {
                    numerator = MovieAList.size();
                    return numerator * 1.0 / denomiator;
                }
            }
    }

    /**
     * 清洗数据集，当用户观看电影总数小于5部时，从训练集,测试集中移除
     * @param relationObjectBase
     */
    private static void DataCleaning(List<RelationObject> relationObjectBase,List<RelationObject> relationObjectTest)
    {
        Map<Integer,Integer> map=new HashMap<Integer, Integer>();
        for(RelationObject relationObject:relationObjectBase)
        {
            if(map.get(relationObject.getUserId())==null)
                map.put(relationObject.getUserId(),1);
            else
                map.put(relationObject.getUserId(),map.get(relationObject.getUserId())+1);
        }
        Iterator<Map.Entry<Integer,Integer>> it=map.entrySet().iterator();
        while(it.hasNext())
        {
            if(it.next().getValue()<=1)
                it.remove();
        }
        Iterator<RelationObject> iterator=relationObjectBase.iterator();
        while(iterator.hasNext())
        {
            if(!map.containsKey(iterator.next().getUserId()))
                iterator.remove();
        }
        Iterator<RelationObject> iterator2=relationObjectTest.iterator();
        while(iterator2.hasNext())
        {
            if(!map.containsKey(iterator2.next().getUserId()))
                iterator2.remove();
        }
    }
}
