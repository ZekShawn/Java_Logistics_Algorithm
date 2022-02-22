package mmpc.common;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Save {

    public static void writeToTxt(String InstanceID, Integer orderFileId, Result result) {

        try {

            // 文件地址保存
            Path path;
            if (InstanceID.contains("L")) {
                path = Paths.get("Results", "Large_" + orderFileId + "_" + InstanceID +"_Result.csv");
            }else{
                path = Paths.get("Results", "Regular" + orderFileId + "_" + InstanceID +"_Result.csv");
            }
            String filePath = path.toString();
            File writeName = new File(filePath);

            // 如果已经存在结果文件，则删除
            try{
                if(writeName.delete()){
                    System.out.println(writeName.getName() + " has been deleted！");
                }else{
                    System.out.println("Failed to delete the file！");
                }
            }catch(Exception e){
                e.printStackTrace();
            }

            // 将内容写入到文件中
            boolean isSucceed = writeName.createNewFile();
            if (isSucceed) System.out.println(writeName.getName() + " has been created！");
            BufferedWriter out = new BufferedWriter(new FileWriter(writeName));
            out.write("orderIndex | solutionRoute | solutionPackage | solutionMoneyCost | solutionTimeCost\r\n");
            for (String orderIndex : result.orderSolutions.keySet()){
                Solution solution = result.orderSolutions.get(orderIndex);
                out.write(orderIndex + " | " + solution.solutionRoute.toString() + " | " + solution.solutionPackage.toString() + " | " + solution.cost + " | " + solution.time +"\r\n");
            }
            out.flush(); // 把缓存区内容压入文件
            out.close();

            // 打印提示信息
            System.out.println("Save the result successfully in " + writeName.toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
