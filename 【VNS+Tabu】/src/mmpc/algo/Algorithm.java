package mmpc.algo;

import mmpc.common.Instance;
import mmpc.common.Result;
import mmpc.common.Solution;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Algorithm {

    public static void run(Instance instance, Result result, Double rate, Integer num, Integer tabuNum) throws IOException, ClassNotFoundException {

        // 计算整个计算过程的时间
        long instanceCostTime = 0;

        // 对 每个订单进行循环
        for (String orderID : result.orderSolutions.keySet()){

            // 查看初始解，打印日志信息
            System.out.println("The current order index is: " + (orderID+1));
            result.orderSolutions.get(orderID).showSolutionLog();

            // 记录单个订单开始时间
            long orderBeginTime = System.currentTimeMillis();

            // 查找最优安排
            int searchCount = variableNeighborhoodSearch(instance, result, tabuNum, orderID, rate, num);

            // 记录单个订单完成时间
            long orderEndTime = System.currentTimeMillis();
            instanceCostTime += (orderEndTime - orderBeginTime);

            // 打印空行信息
            result.orderSolutions.get(orderID).showSolutionLog();
            System.out.println("--------------------------------------split line--------------------------------------");
            System.out.println("The times of search iterator is: " + searchCount + "\n"
                    + "The cost time of order is: " + (orderEndTime - orderBeginTime) + "ms");
            System.out.println("\n");

        }

        // 记录结束时间
        System.out.println("The average cost time of the order: " + (instanceCostTime / result.orderSolutions.size()));

        // 更新 result 订单集的信息
        result.updateParameters();

    }

    public static int variableNeighborhoodSearch(Instance instance, Result result, Integer tabuNum,
                                                  String orderID, Double rate, Integer num)
            throws IOException, ClassNotFoundException{

        // 0.1 Init 邻域集（需要在这里完成）、初始解（已经完成，记录搜索次数
        int searchCount = 0;
        List<List<Map<Integer, List<Integer>>>> neighborAreas = Operators.operator(
                result.orderSolutions.get(orderID).solutionRoute, instance, rate, num);

        // 0.2 记录搜索到的最优解
        Map<Integer, List<Integer>> currentBetsSolutionRoute = result.orderSolutions.get(orderID).solutionRoute;
        double currentBestSummaryCost = result.orderSolutions.get(orderID).summaryCost;
        Map<Integer, List<Integer>> neighborBetsSolutionRoute = Operators.copyValuesOf(neighborAreas.get(0).get(0));
        double neighborBestSummaryCost = getSolutionCost(neighborAreas.get(0).get(0), instance, rate);

        // 1.1 重复直到达到近似全局最优
        while (true){

            // 1.2 设定禁忌搜索次数
            searchCount++;

            // 2. 对每个邻域进行搜索，查找最优解
            for (List<Map<Integer, List<Integer>>> neighborArea : neighborAreas){

                // 2.1 寻找邻域的优解
                Map<Integer, List<Integer>> tempSolutionRoute = searchNeighbors(instance, rate, neighborBestSummaryCost,
                        neighborBetsSolutionRoute, neighborArea);
                double tempSummaryCost = getSolutionCost(tempSolutionRoute, instance, rate);

                // 2.2 查看这次对邻域搜索得到的优解是否比记录的解更优
                if ((tempSummaryCost < neighborBestSummaryCost) && (!tempSolutionRoute.equals(currentBetsSolutionRoute))){

                    // 2.3 如果比当前记录的邻域最优解更优，则更新当前记录的邻域解
                    neighborBetsSolutionRoute = Operators.copyValuesOf(tempSolutionRoute);
                    neighborBestSummaryCost = tempSummaryCost;

                    // 2.4 如果比原来的解更优，则重置禁忌搜索的次数
                    if (neighborBestSummaryCost < currentBestSummaryCost){
                        currentBestSummaryCost = neighborBestSummaryCost;
                        currentBetsSolutionRoute = Operators.copyValuesOf(neighborBetsSolutionRoute);
                        searchCount = 0;
                        break;
                    }
                }
            }

            // 2.5 以记录的邻域最优解为当前解构建邻域
            neighborAreas = Operators.operator(neighborBetsSolutionRoute, instance, rate, num);

            // 3.1 达到终止条件，跳出循环
            if (searchCount >= tabuNum){

                // 3.2 如果比原来的解更优，则更新原来的解
                if (currentBestSummaryCost < result.orderSolutions.get(orderID).summaryCost)
                    result.updateSolution(orderID, currentBetsSolutionRoute, instance, rate);
                break;
            }
        }

        // 返回搜索次数
        return searchCount;
    }

    public static Map<Integer, List<Integer>> searchNeighbors(Instance instance, Double rate, Double currentBestSummaryCost,
                                                              Map<Integer, List<Integer>> currentBestSolutionRoute,
                                                              List<Map<Integer, List<Integer>>> neighborArea) throws IOException, ClassNotFoundException {

        // 定义最佳路径
        Map<Integer, List<Integer>> bestSolutionRoute = currentBestSolutionRoute;

        // 查找当前邻域的最优解
        for (Map<Integer, List<Integer>> tempRoute : neighborArea) {

            // 计算邻域的cost 并存储
            double tempSummaryCost = getSolutionCost(tempRoute, instance, rate);

            // 寻找最小的cost
            if (tempSummaryCost < currentBestSummaryCost) {
                bestSolutionRoute = Operators.copyValuesOf(tempRoute);
                currentBestSummaryCost = tempSummaryCost;
            }
        }

        // 返回邻域中最小 cost 的路径
        return bestSolutionRoute;
    }

    public static Double getSolutionCost(Map<Integer, List<Integer>> solutionRoute, Instance instance, Double rate){

        // Init
        double minSummaryCost;

        // 计算邻域的cost 并存储
        Map<Integer, List<List<Integer>>> tempPackage = Solution.arrangePackage(solutionRoute, instance);
        Map<Integer, Double> tempSKUCost = Solution.calculateSolutionCost(solutionRoute, instance);
        Map<Integer, Double> tempSKUTime = Solution.calculateSolutionTime(solutionRoute, instance);
        double tempCost = Solution.calculateCost(tempSKUCost, tempPackage, instance);
        double tempTime = Solution.calculateTime(tempSKUTime);
        minSummaryCost = Solution.calculateSummaryCost(tempCost, tempTime, rate);
        return minSummaryCost;
    }
}
