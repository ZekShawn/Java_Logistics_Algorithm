package mmpc.common;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Instance {

    /*
     * 动态数组转换为静态数组
     * int size=list.size();
     * Double[] array = (Double[])list.toArray(new String[size]);
     * */

    /*
     ["1-2", "2-3", "3-5"]
     ware1-ware2
     String value = String.valueOf(ware1) + "-" + String.valueOf(ware2);
     */

    public Map<String, List<Double>> BetweenWarehouses;
    public Map<String, List<Integer>> Orders;
    public Map<Integer, Integer> SKUs;
    public List<String> SKUsConflicts;
    public Map<Integer, List<Double>> Warehouses;

    public Instance(String InstanceID, String InstanceIndex){
        this.BetweenWarehouses = setBetweenWarehouses(InstanceID);
        this.Orders = setOrders(InstanceID, InstanceIndex);
        this.SKUs = setSKUs(InstanceID);
        this.SKUsConflicts = setSKUsConflicts(InstanceID);
        this.Warehouses = setWarehouses(InstanceID);
    }

    public Map<String, List<Double>> setBetweenWarehouses(String InstanceID){
        Map<String, List<Double>> BetweenWarehouses = new HashMap<>();
        Map<String, List<String>> between = getOthers(InstanceID, "BetweenWarehouses");
        List<String> tempWarehouseID1 = between.get("WarehouseID1");
        List<String> tempWarehouseID2 = between.get("WarehouseID2");
        List<String> tempTransshipmentCost = between.get("TransshipmentCost");
        List<String> tempTranshipmentTime = between.get("TranshippmentTime");
        for (int i=0;i<tempWarehouseID1.size();i++){
            String key = tempWarehouseID1.get(i).substring(1) + "-" + tempWarehouseID2.get(i).substring(1);
            List<Double> tempList = new ArrayList<>();
            Double valueCost = Double.parseDouble(tempTransshipmentCost.get(i));
            Double valueTime = Double.parseDouble(tempTranshipmentTime.get(i));
            tempList.add(valueCost);
            tempList.add(valueTime);
            BetweenWarehouses.put(key, tempList);
        }
        return BetweenWarehouses;
    }

    public Map<String, List<Integer>> setOrders(String InstanceID, String InstanceIndex){
        Map<String, List<Integer>> Orders = new HashMap<>();
        Map<String, List<String>> order = getOrders(InstanceID, InstanceIndex);
        List<String> SkuID = order.get("SkuID");
        List<String> OrderType = order.get("OrderType");
        List<String> OrderID = order.get("OrderID");
        for (int i=0;i<SkuID.size();i++){
            if (OrderType.get(i).equals("Father")){
                String[] temp_order = SkuID.get(i).split(",");
                List<Integer> temp_skus = new ArrayList<>();
                for (String sku: temp_order){
                    temp_skus.add(Integer.parseInt(sku.substring(1)));
                }
                Orders.put(OrderID.get(i), temp_skus);
            }
        }
        return Orders;
    }

    public Map<Integer, Integer> setSKUs(String InstanceID){
        Map<Integer, Integer> SKUs = new HashMap<>();
        Map<String, List<String>> sku = getOthers(InstanceID, "SKUs");
        List<String> tempSkuID = sku.get("SkuID");
        List<String> tempWarehouse = sku.get("Warehouse");
        for (int i=0;i<tempSkuID.size();i++){
            SKUs.put(Integer.parseInt(tempSkuID.get(i).substring(1)), Integer.parseInt(tempWarehouse.get(i).substring(1)));
        }
        return SKUs;
    }

    public List<String> setSKUsConflicts(String InstanceID){
        List<String> SKUsConflicts = new ArrayList<>();
        Map<String, List<String>> sku = getOthers(InstanceID, "SKUsConflicts");
        List<String> tempSkuID1 = sku.get("SkuID1");
        List<String> tempSkuID2 = sku.get("SkuID2");
        for (int i=0; i<tempSkuID1.size(); i++){
            String conflict = tempSkuID1.get(i).substring(1) + "-" + tempSkuID2.get(i).substring(1);
            SKUsConflicts.add(conflict);
        }
        return SKUsConflicts;
    }

    public Map<Integer, List<Double>> setWarehouses(String InstanceID){
        Map<Integer, List<Double>> Warehouses = new HashMap<>();
        Map<String, List<String>> ware = getOthers(InstanceID, "Warehouses");
        List<String> tempWarehouseID = ware.get("WarehouseID");
        List<String> tempFixedShipCost = ware.get("FixedShipCost");
        List<String> tempVariableShipCost = ware.get("VariableShipCost");
        List<String> tempShipTime = ware.get("ShipTime");
        for (int i=0;i<tempWarehouseID.size();i++){
            List<Double> temp_list = new ArrayList<>();
            temp_list.add(Double.parseDouble(tempFixedShipCost.get(i)));
            temp_list.add(Double.parseDouble(tempVariableShipCost.get(i)));
            temp_list.add(Double.parseDouble(tempShipTime.get(i)));
            Warehouses.put(Integer.parseInt(tempWarehouseID.get(i).substring(1)), temp_list);
        }
        return Warehouses;
    }

    public static Map<String, List<String>> getOthers(String InstanceID, String DataType){
        String InstanceType;
        if (InstanceID.contains("R")){
            InstanceType = "Regular Instances";
        }else{
            InstanceType = "Large Instances";
        }
        Path FilePath = Paths.get("Instances", InstanceType, DataType);
        FilePath = Paths.get(FilePath.toString(), InstanceID+"_"+DataType+".txt");
        return read_txt(FilePath.toString(), ",", InstanceID);
    }

    public static Map<String, List<String>> getOrders(String InstanceID, String InstanceIndex){
        String InstanceType;
        String Instance;
        if (InstanceID.contains("R")){
            InstanceType = "Regular Instances";
            Instance = "Regular";
        }else{
            InstanceType = "Large Instances";
            Instance = "Large";
        }
        Path FilePath = Paths.get("Instances", InstanceType, "Orders");
        FilePath = Paths.get(FilePath.toString(), Instance + "_" + InstanceIndex + "_" + InstanceID + "_Orders.txt");
        return read_txt(FilePath.toString(), ";", InstanceID);
    }

    public static Map<String, List<String>> read_txt(String pathname, String sep, String symbol){
        List<String> columns = new ArrayList<>(); // List初始化
        List<List<String>> data = new ArrayList<>();
        Map<String, List<String>> warehouses = new HashMap<>();
        try{
            File filename = new File(pathname);
            InputStreamReader reader = new InputStreamReader(new FileInputStream(filename));
            BufferedReader br = new BufferedReader(reader);
            String line;
            line = br.readLine();
            while (line != null) {
                if (line.contains("Columns")){
                    columns = Arrays.asList(line.split(","));
                    columns.set(0, columns.get(0).substring(9));
                    for (int i=0;i<columns.size();i++){
                        columns.set(i, columns.get(i).trim());
                    }
                }else if (line.contains(symbol)){
                    List<String> temp_data = Arrays.asList(line.split((sep)));
                    for (int i=0;i<temp_data.size();i++){
                        temp_data.set(i, temp_data.get(i).trim());
                    }
                    data.add(temp_data);
                }
                line = br.readLine();
            }
            data.remove(0);
            for (int i=0;i<columns.size();i++){
                List<String> temp_warehouse = new ArrayList<>();
                for (List<String> temp_data: data){
                    temp_warehouse.add(temp_data.get(i));
                }
                warehouses.put(columns.get(i), temp_warehouse);
            }
            return warehouses;

        }catch (IOException e){
            throw new RuntimeException(e);
        }
    }
}

