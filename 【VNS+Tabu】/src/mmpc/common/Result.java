package mmpc.common;

import mmpc.algo.Operators;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Result {

    // 解列表
    public Map<String, Solution> orderSolutions = new HashMap<>();
    // cost 列表
    public Map<String, Double> orderCosts = new HashMap<>();
    // 所有订单的总cost
    public Double allCost = 0.0;
    // time 列表
    public Map<String, Double> orderTime = new HashMap<>();

    public Result(Instance instance, Double rate){
        this.calculateOrderSolutions(instance, rate);
        this.calculateOrderCost();
        this.calculateCost();
        this.calculateTime();
    }

    public void updateSolution(String orderID, Map<Integer, List<Integer>> bestSolutionRoute,
                               Instance instance, Double rate)
            throws IOException, ClassNotFoundException {
        this.orderSolutions.get(orderID).solutionRoute = Operators.copyValuesOf(bestSolutionRoute);
        this.orderSolutions.get(orderID).solutionPackage = Solution.arrangePackage(bestSolutionRoute, instance);
        this.orderSolutions.get(orderID).SKUCost = Solution.calculateSolutionCost(bestSolutionRoute, instance);
        this.orderSolutions.get(orderID).SKUTime = Solution.calculateSolutionTime(bestSolutionRoute, instance);
        this.orderSolutions.get(orderID).cost = Solution.calculateCost(this.orderSolutions.get(orderID).SKUCost,
                this.orderSolutions.get(orderID).solutionPackage, instance);
        this.orderSolutions.get(orderID).time = Solution.calculateTime(this.orderSolutions.get(orderID).SKUTime);
        this.orderSolutions.get(orderID).summaryCost = Solution.calculateSummaryCost(
                this.orderSolutions.get(orderID).cost, this.orderSolutions.get(orderID).time, rate);
    }

    public void updateParameters(){
        this.calculateOrderCost();
        this.calculateCost();
        this.calculateTime();
    }

    public void calculateOrderSolutions(Instance instance, Double rate){
        for (String orderIndex : instance.Orders.keySet()){
            Solution solution = new Solution(instance, orderIndex, rate);
            this.orderSolutions.put(orderIndex, solution);
        }
    }

    public void calculateOrderCost(){
        this.orderCosts = new HashMap<>();
        for (String orderIndex: this.orderSolutions.keySet()){
            this.orderCosts.put(orderIndex, this.orderSolutions.get(orderIndex).cost);
        }
    }

    public void calculateCost(){
        this.allCost = 0.0;
        for (Double value: this.orderCosts.values()){
            this.allCost += value;
        }
    }

    public void calculateTime(){
        this.orderTime = new HashMap<>();
        for (String orderIndex : this.orderSolutions.keySet()){
            this.orderTime.put(orderIndex, this.orderSolutions.get(orderIndex).time);
        }
    }
}
