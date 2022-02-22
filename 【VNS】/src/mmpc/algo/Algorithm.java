package mmpc.algo;

import mmpc.common.Instance;
import mmpc.common.Result;
import mmpc.common.Solution;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class Algorithm {

    public static void run(Instance instance, Result result, Double rate, Integer num) throws IOException, ClassNotFoundException {

        // 计算整个计算过程的时间
        long instanceCostTime = 0;

        // 对 每个订单进行循环
        for (String orderID : result.orderSolutions.keySet()){

            // 查看初始解，打印日志信息
            System.out.println("The current order index is: " + orderID);
            result.orderSolutions.get(orderID).showSolutionLog();

            // 记录单个订单开始时间
            long orderBeginTime = System.currentTimeMillis();

            // 查找最优安排
            int searchCount = variableNeighborhoodSearch(instance, result, orderID, rate, num);

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

    public static int variableNeighborhoodSearch(Instance instance, Result result,
                                                  String orderID, Double rate, Integer num)
            throws IOException, ClassNotFoundException{

        // 0. Init 邻域集（需要在这里完成）、初始解（已经完成，记录搜索次数
        int searchCount = 1;
        List<List<Map<Integer, List<Integer>>>> neighborAreas = Operators.operator(
                result.orderSolutions.get(orderID).solutionRoute, instance, rate, num);

        // 1. 重复直到达到近似全局最优
        while (true){

            // 设定终止条件，记录迭代次数
            boolean terminate = true;
            searchCount++;

            // 2. 对每个邻域进行搜索，查找最优解
            for (List<Map<Integer, List<Integer>>> neighborArea : neighborAreas){

                // 2.1 记录当前最优解信息
                double currentCost = result.orderSolutions.get(orderID).summaryCost;
                Map<Integer, List<Integer>> currentSolutionRoute =
                        Operators.copyValuesOf(result.orderSolutions.get(orderID).solutionRoute);

                // 2.2 寻找最优
                double neighborCost = searchNeighbors(instance, result, orderID, rate, neighborArea);
                Map<Integer, List<Integer>> optimalSolutionRoute =
                        Operators.copyValuesOf(result.orderSolutions.get(orderID).solutionRoute);

                // 2.3 查看是否更优
                if ((neighborCost < currentCost) && (!currentSolutionRoute.equals(optimalSolutionRoute))){

                    // 2.4 如果是更优的，则更新邻域，且跳出循环，终止条件设为否
                    neighborAreas = Operators.operator(
                            result.orderSolutions.get(orderID).solutionRoute, instance, rate, num);
                    terminate = false;
                    break;
                }
            }

            // 3. 达到终止条件，跳出循环
            if (terminate){
                break;
            }
        }

        // 返回搜索次数
        return searchCount;
    }

    public static Double searchNeighbors(Instance instance, Result result, String orderID,
                                           Double rate, List<Map<Integer, List<Integer>>> neighborArea) {

        // 初始化当前解为最优
        Map<Integer, List<Integer>> minCostRoute = result.orderSolutions.get(orderID).solutionRoute;
        Map<Integer, List<List<Integer>>> minCostPackage = result.orderSolutions.get(orderID).solutionPackage;
        Map<Integer, Double> minCostSKUCost = result.orderSolutions.get(orderID).SKUCost;
        Map<Integer, Double> minTimeSKUTime = result.orderSolutions.get(orderID).SKUTime;
        double minCost = result.orderSolutions.get(orderID).cost;
        double minTime = result.orderSolutions.get(orderID).time;
        double minSummaryCost = result.orderSolutions.get(orderID).summaryCost;

        // 查找当前邻域的最优解
        for (Map<Integer, List<Integer>> tempRoute : neighborArea) {

            // 计算邻域的cost 并存储
            Map<Integer, List<List<Integer>>> tempPackage = Solution.arrangePackage(tempRoute, instance);
            Map<Integer, Double> tempSKUCost = Solution.calculateSolutionCost(tempRoute, instance);
            Map<Integer, Double> tempSKUTime = Solution.calculateSolutionTime(tempRoute, instance);
            double tempCost = Solution.calculateCost(tempSKUCost, tempPackage, instance);
            double tempTime = Solution.calculateTime(tempSKUTime);
            double tempSummaryCost = Solution.calculateSummaryCost(tempCost, tempTime, rate);

            // 寻找最小的cost
            if (tempSummaryCost < minSummaryCost) {
                minCostRoute = tempRoute;
                minCostPackage = tempPackage;
                minCostSKUCost = tempSKUCost;
                minTimeSKUTime = tempSKUTime;
                minCost = tempCost;
                minTime = tempTime;
                minSummaryCost = tempSummaryCost;
            }
        }

        // 如果找到的解更优，则更新当前解的信息
        if (minSummaryCost < result.orderSolutions.get(orderID).summaryCost){
            result.orderSolutions.get(orderID).solutionRoute = minCostRoute;
            result.orderSolutions.get(orderID).solutionPackage = minCostPackage;
            result.orderSolutions.get(orderID).SKUCost = minCostSKUCost;
            result.orderSolutions.get(orderID).SKUTime = minTimeSKUTime;
            result.orderSolutions.get(orderID).cost = minCost;
            result.orderSolutions.get(orderID).time = minTime;
            result.orderSolutions.get(orderID).summaryCost = minSummaryCost;
        }

        // 返回邻域中最小的cost
        return minSummaryCost;
    }
}
