/**
 * Author: Qiu Xiangzhi
 * Date: 2021-06-03 -> 2021-06-11
 * Version: 1.0
 * Method: Variable Neighborhood Search
 * Problem: MMPC
 * Result file sep: " | "
 * Default test instance: Large instance
 */

import mmpc.common.Instance;
import mmpc.common.Result;
import mmpc.algo.Algorithm;
import mmpc.common.Save;

import java.io.IOException;

public class Main {

    public static double rate = 1;
    public static int num = 5;
    public static int maxIndex = 5;
    public static int maxInstanceIndex = 6;
    public static String instanceCategory = "L";

    public static void main(String[] args) throws IOException, ClassNotFoundException {
        run();
    }

    public static void run() throws IOException, ClassNotFoundException {
        for (int i=1; i<maxIndex; i++){
            for (int j=1; j<maxInstanceIndex; j++){
                Instance instance = new Instance(instanceCategory + j, String.valueOf(i));        // 收取 样例 信息
                Result result = new Result(instance, rate);       // 构建并初始化解的信息
                Algorithm.run(instance, result, rate, num);            // 跑算法得到结果
                Save.writeToTxt(instanceCategory + j, i, result);
            }
        }
    }
}