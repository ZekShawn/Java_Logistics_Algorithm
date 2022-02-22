package mmpc.algo;

import mmpc.common.Instance;
import mmpc.common.Solution;

import java.io.*;
import java.util.*;

public class Operators {

    // 获得邻域集
    public static List<List<Map<Integer, List<Integer>>>> operator(Map<Integer, List<Integer>> solutionRoute,
                                                                   Instance instance, Double rate, int num)
            throws IOException, ClassNotFoundException {

        // 初始化(1+n)个邻域
        List<List<Map<Integer, List<Integer>>>> neighborAreas = new ArrayList<>();
        neighborAreas.add(operator1(solutionRoute, instance.Warehouses));

        Map<Double, Integer> neighborArea1Cost = getNeighborAreaCost(neighborAreas.get(0), instance, rate);
        neighborAreas.add(operator2(neighborAreas.get(0), neighborArea1Cost, num, instance.Warehouses));

        // 返回邻域集
        return neighborAreas;
    }

    // 获取邻域1的cost - index
    public static Map<Double, Integer> getNeighborAreaCost(List<Map<Integer, List<Integer>>> neighborArea,
                                                           Instance instance, Double rate) {

        // 初始化数据，存储每一个邻域的cost与邻域中的index，查找当前邻域的最优解
        Map<Double, Integer> neighborAreaCost = new HashMap<>();
        for (Map<Integer, List<Integer>> tempRoute : neighborArea) {

            // 计算邻域的cost 并存储
            Map<Integer, List<List<Integer>>> tempPackage = Solution.arrangePackage(tempRoute, instance);
            Map<Integer, Double> tempSKUCost = Solution.calculateSolutionCost(tempRoute, instance);
            Map<Integer, Double> tempSKUTime = Solution.calculateSolutionTime(tempRoute, instance);
            Double tempCost = Solution.calculateCost(tempSKUCost, tempPackage, instance);
            Double tempTime = Solution.calculateTime(tempSKUTime);
            Double tempSummaryCost = Solution.calculateSummaryCost(tempCost, tempTime, rate);
            neighborAreaCost.put(tempSummaryCost, neighborArea.indexOf(tempRoute));
        }

        // 返回 cost-index
        return neighborAreaCost;
    }

    // 算子1
    public static List<Map<Integer, List<Integer>>> operator1(Map<Integer, List<Integer>> solutionRoute,
                                                              Map<Integer, List<Double>> warehouses)
            throws IOException, ClassNotFoundException {

        // 初始化邻域1
        List<Map<Integer, List<Integer>>> neighborArea = new ArrayList<>();
        for (Integer SKU : solutionRoute.keySet()){
            int lastWarehouseIndex = solutionRoute.get(SKU).size()-1;
            Integer lastWarehouse = solutionRoute.get(SKU).get(lastWarehouseIndex);
            for (Integer warehouse : warehouses.keySet()){
                if (!lastWarehouse.equals(warehouse)){
                    Map<Integer, List<Integer>> tempSolutionRoute = copyValuesOf(solutionRoute);
                    tempSolutionRoute.get(SKU).add(warehouse);
                    neighborArea.add(tempSolutionRoute);
                }
            }
        }
        
        // 返回邻域1
        return neighborArea;
    }

    // 算子2，基于算子1进一步的邻域操作
    public static List<Map<Integer, List<Integer>>> operator2(List<Map<Integer, List<Integer>>> neighborArea,
                                                              Map<Double, Integer> neighborCost, int num,
                                                              Map<Integer, List<Double>> warehouses)
            throws IOException, ClassNotFoundException {

        // 初始化新的邻域 为 第一个领域计算出的cost从小到大排列前50的解
        List<Map<Integer, List<Integer>>> solutionRouteList = new ArrayList<>();
        Iterator<Double> iterator = neighborCost.keySet().iterator();
        int iteratorTimes = 0;
        while (iterator.hasNext()) {
            iteratorTimes++;
            Double key = iterator.next();
            // if (iteratorTimes < (num/2) || iteratorTimes > (neighborCost.keySet().size() - num/2)){
            if (iteratorTimes < num){
                int neighborIndex = neighborCost.get(key);
                solutionRouteList.add(copyValuesOf(neighborArea.get(neighborIndex)));
            }
        }

        // 循环每一个新的邻域解，并以此生成新的邻域
        List<Map<Integer, List<Integer>>> newNeighborArea = new ArrayList<>();
        for (Map<Integer, List<Integer>> solutionRoute : solutionRouteList){
            List<Map<Integer, List<Integer>>> tempNeighborArea = operator1(solutionRoute, warehouses);
            newNeighborArea.addAll(tempNeighborArea);
        }

        // 返回邻域2
        return newNeighborArea;
    }

    // 手动拷贝尝试
    public static Map<Integer, List<Integer>> copyValuesOf(Map<Integer, List<Integer>> solutionRoute)
            throws IOException, ClassNotFoundException {
        Map<Integer, List<Integer>> newSolutionRoute = new HashMap<>();
        for (Integer key : solutionRoute.keySet()){
            newSolutionRoute.put(key, deepCopy(solutionRoute.get(key)));
        }
        return newSolutionRoute;
    }

    // 拷贝 List - 深拷贝
    public static List<Integer> deepCopy(List<Integer> src) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(src);

        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteOut.toByteArray());
        ObjectInputStream in = new ObjectInputStream(byteIn);
        @SuppressWarnings("unchecked")
        List<Integer> dest = (List<Integer>) in.readObject();
        return dest;
    }

}