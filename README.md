## Code目录结构

- 【NS+Dijkstra】：邻域搜索+Dijkstra初始化
- 【VNS】：变邻域搜索
- 【VNS+Tabu】：结合禁忌思想的变邻域搜索；

## VNS / VNS+Tabu 目录结构

```bash
Code/【VNS+Tabu】
├── Instances
├── Results
│   └── Large_1_L1_Result.csv
├── out
└── src
    ├── Main.java
    └── mmpc
        ├── algo
        │   ├── Algorithm.java
        │   └── Operators.java
        └── common
            ├── Instance.java
            ├── Result.java
            ├── Save.java
            └── Solution.java
```

- Instance：测试样例数据；
- Results：测试结果数据；
- out：IDE的输出文件；
- src：算法源文件；
- Results文件名规则：`测试样例规模_测试用例序号_测试用例编号_Result.csv`，如：
    - `Large_1_L1_Result.csv / Regular_1_R1_Result.csv`；

## 【NS+Dijkstra】目录结构

```bash
Code/【NS+Dijkstra】
├── Instances
├── out
├── src
│   ├── Algorithm.java
│   ├── Dijkstra.java
│   ├── Edge.java
│   ├── Instance.java
│   ├── Main.java
│   ├── Order.java
│   ├── Orders.java
│   ├── SKU.java
│   ├── Vertex.java
│   ├── graph.java
│   └── init.java
```

- Instance：测试样例数据；
- out：IDE的输出文件；
- src：算法源文件；

## 测试环境

如运行出错，可参考下列测试通过的运行环境：

- 操作系统：Windows 10 / macOS Big Sur+  + JDK 1.8 / JDK 11.0；
- IDE：Intellij IDEA CE；
- JDK版本：1.8 / 11.0；

## 运行方式

- 将主目录下的`Instance`文件夹覆盖复制到`Code/[算法目录]/Instance`；

- 以`Code/[算法目录]`为项目主目录打开，运行`Code/[算法目录]/src/Main.java`；

- 结果将保存在`Results`目录；

    - 【NS+Dijkstra】算法的结果将只在控制台输出，不输出到文件；

- 可调整参数及其说明：

    - 参数的调整位于Main.java文件中；

    - ```java
        // 参数说明
        public static double rate = 1; // 目标函数中的时间惩罚系数；
        public static int num = 3; // 挑选第一个邻域前num个目标值最小的邻域解生成第二个邻域；
        public static int tabuNum = 10; // 禁忌步长，即允许在邻域最优解不优于当前解多少步的情况下继续往前搜索；
        public static int maxIndex = 5; // 测试用例相关，为Orders文件中的第一位数组编号最大值+1
        public static int maxInstanceIndex = 6; // 测试用例相关，为Orders文件中的第二位数组编号最大值+1
        public static String instanceCategory = "L"; // 测试用例规模选择，Large还是Regular，可选“L”与“R”；
        ```

    - `num`参数可结合算子生成部分理解；

