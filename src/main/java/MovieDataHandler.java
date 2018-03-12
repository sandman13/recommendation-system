import com.google.common.collect.Lists;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.List;

/**
 * @author yutong song
 * @date 2018/3/4
 * 提供url,解析movie数据,返回关联列表
 */
public class MovieDataHandler {

    public  static List<RelationObject> handle(String url) throws Exception {
        List<RelationObject> relationObjectList = Lists.newArrayList();
        FileReader fileReader = new FileReader(url);
        BufferedReader br = new BufferedReader(fileReader);
        String line = null;
        while ((line = br.readLine()) != null) {
            line=line.replace((char)9,' ');
            String[] temp = line.split(" ");
            RelationObject relationObject = new RelationObject();
            relationObject.setUserId(Integer.valueOf(temp[0]));
            relationObject.setItemId(Integer.valueOf(temp[1]));
            relationObject.setRate(Double.valueOf(temp[2]));
            relationObject.setTimePoint(Long.valueOf(temp[3]));
            relationObjectList.add(relationObject);
        }
       return relationObjectList;
}

    public static void main(String[] args) throws Exception {
        System.out.println(new MovieDataHandler().handle(MovieDataHandler.class.getResource("/").getPath() + "u1.base"));
    }

}
