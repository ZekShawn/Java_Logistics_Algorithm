package mmpc.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Solution {

    // 订单的编号
    public String orderIndex;

    // 当前解 - 每一个SKU对应的路线组成的集合
    public Map<Integer, List<Integer>> solutionRoute;
    // 当前解 - 仓库点的包裹集
    public Map<Integer, List<List<Integer>>> solutionPackage;

    // 当前解的每一个SKU对应的cost，不计算是否合并
    public Map<Integer, Double> SKUCost;
    // 当前解的每一个SKU对应的time，不计算是否合并
    public Map<Integer, Double> SKUTime;

    // 当前解的总cost，计算是否合并
    public Double cost;
    // 当前解的总time，计算是否合并
    public Double time;

    // 综合 Cost 衡量标准
    public Double summaryCost;

    // 构造函数
    public Solution(Instance instance, String orderIndex, Double rate){

        this.orderIndex = orderIndex;

        this.solutionRoute = primarySolution(instance.Orders.get(orderIndex), instance);
        this.solutionPackage = arrangePackage(this.solutionRoute, instance);
        this.SKUCost = calculateSolutionCost(this.solutionRoute, instance);
        this.SKUTime = calculateSolutionTime(this.solutionRoute, instance);
        this.cost = calculateCost(this.SKUCost, this.solutionPackage, instance);
        this.time = calculateTime(this.SKUTime);

        this.summaryCost = calculateSummaryCost(this.cost, this.time, rate);
    }

    // 生成初始解
    public Map<Integer, List<Integer>> primarySolution(List<Integer> order, Instance instance){
        Map<Integer, List<Integer>> Solution = new HashMap<>();
        for (Integer SKU: order){
            List<Integer> tempSKURoute = new ArrayList<>();
            tempSKURoute.add(instance.SKUs.get(SKU));
            Solution.put(SKU, tempSKURoute);
        }
        return Solution;
    }

    // 计算解中每一个SKU的Cost，不带最终的固定成本与运输成本
    public static Map<Integer, Double> calculateSolutionCost(Map<Integer, List<Integer>> solution, Instance instance){
        Map<Integer, Double> SKUCost = new HashMap<>();
        for (Integer key: solution.keySet()){
            List<Integer> tempSKURoute = solution.get(key);
            Double tempRouteCost = 0.0;
            for (int i=0;i<tempSKURoute.size();i++){
                if (tempSKURoute.size() == i+1){
                    break;
                }
                String tempKey = getBetweenWarehousesKey(tempSKURoute.get(i), tempSKURoute.get(i+1));
                tempRouteCost += instance.BetweenWarehouses.get(tempKey).get(0);
            }
            SKUCost.put(key, tempRouteCost);
        }
        return SKUCost;
    }

    // 计算解中每一个SKU花费的Time，带最后的运输时间
    public static Map<Integer, Double> calculateSolutionTime(Map<Integer, List<Integer>> solutionRoute, Instance instance){
        Map<Integer, Double> SKUTime = new HashMap<>();
        for (Integer key: solutionRoute.keySet()){
            List<Integer> tempSKURoute = solutionRoute.get(key);
            Double tempRouteTime = 0.0;
            for (int i=0; i<tempSKURoute.size()-1; i++){
                String tempKey = getBetweenWarehousesKey(tempSKURoute.get(i), tempSKURoute.get(i+1));
                tempRouteTime += instance.BetweenWarehouses.get(tempKey).get(1);
            }
            Integer finalKey = tempSKURoute.get(tempSKURoute.size()-1);
            tempRouteTime += instance.Warehouses.get(finalKey).get(2);
            SKUTime.put(key, tempRouteTime);
        }
        return SKUTime;
    }

    // 计算最优解的总cost
    public static Double calculateCost(Map<Integer, Double> SKUCost, Map<Integer, List<List<Integer>>> solutionPackage,
                                       Instance instance){
        Double cost = 0.0;
        for (Double value: SKUCost.values()){
            cost += value;
        }
        for (Integer key : solutionPackage.keySet()){
            int num = solutionPackage.get(key).size();
            int SKUCounts = 0;
            for (List<Integer> pack : solutionPackage.get(key)){
                SKUCounts += pack.size();
            }
            cost += instance.Warehouses.get(key).get(0) * num + instance.Warehouses.get(key).get(1) * SKUCounts;
        }
        return cost;
    }

    // 计算最优解的总Time
    public static Double calculateTime(Map<Integer, Double> SKUTime){
        Double time = 0.0;
        for (Integer key : SKUTime.keySet()){
            if (SKUTime.get(key) > time){
                time = SKUTime.get(key);
            }
        }
        return time;
    }

    // 计算综合成本
    public static Double calculateSummaryCost(Double cost, Double time, Double rate){
        return cost + time * rate;
    }

    // 判断 SKU 是否与 List 中的元素冲突
    public static boolean ifConflict(Integer SKU, List<Integer> pack, Instance instance){
        boolean conflict = false;
        for (Integer sku : pack){
            if (instance.SKUsConflicts.contains(SKU + "-" + sku) ||
                    instance.SKUsConflicts.contains((sku + "-" + SKU))){
                conflict = true;
                break;
            }
        }
        return conflict;
    }

    // 获得仓库的key，由最小到最大的方式
    public static String getBetweenWarehousesKey(int a, int b){
        if (a < b) return a + "-" + b;
        else return b + "-" + a;
    }

    // 安排每个SKU的Package， Warehouse - Package
    public static Map<Integer, List<List<Integer>>> arrangePackage(Map<Integer, List<Integer>> solutionRoute, Instance instance){
        Map<Integer, List<List<Integer>>> solutionPackage = new HashMap<>();
        // 循环查找所有的仓库
        Map<Integer, List<Integer>> warehouseSKU = new HashMap<>();
        // 将每一个SKU的最后一个仓库点作为键，SKU作为值加入对应的list
        for (Integer key : solutionRoute.keySet()){
            int lastWarehouseIndex = solutionRoute.get(key).size() - 1;
            Integer lastWarehouse = solutionRoute.get(key).get(lastWarehouseIndex);
            if (warehouseSKU.containsKey(lastWarehouse)){
                warehouseSKU.get(lastWarehouse).add(key);
            }else{
                List<Integer> tempSKUs = new ArrayList<>();
                tempSKUs.add(key);
                warehouseSKU.put(lastWarehouse, tempSKUs);
            }
        }
        // 遍历每一个仓库节点，查找
        for (Integer key : warehouseSKU.keySet()){
            List<List<Integer>> packageList = new ArrayList<>();
            packageList.add(new ArrayList<>());
            List<Integer> tempPackage = warehouseSKU.get(key);
            // 遍历该仓库点，所有经过的最终SKU，对其进行分包
            while (tempPackage.size() > 0){
                Integer SKU = tempPackage.get(0);
                // 查看包裹内的SKU是否存在冲突
                for (int i=0; i<packageList.size(); i++){
                    if (!ifConflict(SKU, packageList.get(i), instance)){
                        packageList.get(i).add(SKU);
                        tempPackage.remove(SKU);
                        break;
                    }else{
                        packageList.add(new ArrayList<>());
                    }
                }
            }
            solutionPackage.put(key, packageList);
        }
        return solutionPackage;
    }

    // 展示解的情况
    public void showSolutionLog(){
        System.out.println("--------------------------------------split line--------------------------------------");
        System.out.println("The current route: \n" + this.solutionRoute);
        System.out.println("The current package list: \n" + this.solutionPackage);
        System.out.println("The current cost of the best plan is: " + this.cost);
        System.out.println("The current ship time of the best plan is: " + this.time);
        System.out.println("The current summary cost of the best plan is: " + this.summaryCost);
    }
}
