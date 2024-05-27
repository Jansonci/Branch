import ilog.concert.*;
import ilog.cplex.IloCplex;
import java.util.Comparator;
import java.util.PriorityQueue;

class ModelData{
    //目标系数
    double[] objectiveCoefficient={1,5};
    //约束系数
    double[][] constraintCoefficient={{-1,1},{5,6},{1,0},{-1,0},{0,-1}};
    //约束值
    double[] constraintValue={2,30,4,0,0};
    //变量数量
    int variableNumber=2;
    //约束数量
    int constrainNumber=5;
}

class Node {
    ModelData data;
    double nodeObj;//当前节点的目标值
    double[] nodeResult;//当前节点的解
    int LSpc;//当前节点分支时左支所添加的约束方程的特征值，如若添加的约束方程为x1<=2，那此特征值即为2
    int RSpc;//当前节点分支时右支所添加的约束方程的特征值，如若添加的约束方程为x1>=3，那此特征值即为3
    int SourceVar;//当前节点的父节点当初分支时选取的变量的序号，如若父节点当初是选用x1进行分支，那此值即为1
    int SourceDir;//指示当前节点是其父节点的左孩子还是右孩子，0代表左，1代表右
    int[][] LAncSpec;//储存当前节点所有祖先节点中所有身份为左孩子（即由左支得到）的祖先节点的变量序号和特征值
    int[][] RAncSpec;//储存当前节点所有祖先节点中所有身份为右孩子（即由右支得到）的祖先节点的变量序号和特征值
    Node father;//此节点的父亲节点
    private Node leftChild;  //左孩子
    private Node rightChild; //右孩子

    public ModelData getData() {
        return data;
    }

    public Node getLeftChild() {
        return leftChild;
    }

    public void setLeftChild(Node leftChild) {
        this.leftChild = leftChild;
    }

    public Node getRightChild() {
        return rightChild;
    }

    public void setRightChild(Node rightChild) {
        this.rightChild = rightChild;
    }

    public Node(ModelData data) {
        this.data = data;
        nodeResult = new double[data.variableNumber];
    }

    public Node nodeCopy() {
        Node newNode = new Node(data);
        newNode.nodeObj = nodeObj;
        newNode.nodeResult = nodeResult.clone();
        return newNode;
    }
}

class BinaryTree {
      private Node root;
      Node findNode(Node root, ModelData x){
         if(root == null){
             return null;
         }
         else if(root.getData() == x){
             return root;
         }
         Node leftNode = findNode(root.getLeftChild(), x);
         if(null != leftNode)
             return leftNode;
         Node rightNode = findNode(root.getRightChild(), x);
         if(null != rightNode)
             return rightNode;
         return null;
     }

    public BinaryTree(){}

    public void setRoot(Node root){
        this.root = root;
    }
}

public class BranchAndBoundDemo {
    ModelData data;
    Node node1, node2;
    Node[] Nd;
    double curBest;
    Node curBestNode;
    PriorityQueue<Node> queue = new PriorityQueue<>(new Comparator<Node>() {
        @Override
        public int compare(Node o1, Node o2) {
            return o1.nodeObj > o2.nodeObj ? -1 : 1;
        }
    });
    BinaryTree tree = new BinaryTree();
    IloCplex model;
    IloNumVar[] x;
    double[] xValue;
    double modelObj;

    public BranchAndBoundDemo(ModelData data) {
        this.data = data;
        xValue = new double[data.variableNumber];
    }

    private void buildModel() throws IloException {
        model = new IloCplex();
        model.setOut(null);
        x = new IloNumVar[data.variableNumber];
        for (int i = 0; i < data.variableNumber; i++) {
            x[i] = model.numVar(-1, 1e15, IloNumVarType.Float, "x[" + i + "]");
        }
        IloNumExpr obj = model.numExpr();
        for (int i = 0; i < data.variableNumber; i++) {
            obj = model.sum(obj, model.prod(data.objectiveCoefficient[i], x[i]));
        }
        model.addMaximize(obj);
        for (int k = 0; k < data.constrainNumber; k++) {
            IloNumExpr expr = model.numExpr();
            for (int i = 0; i < data.variableNumber; i++) {
                expr = model.sum(expr, model.prod(data.constraintCoefficient[k][i], x[i]));
            }
            model.addLe(expr, data.constraintValue[k]);
        }
    }

    private void solveModel() throws IloException {
        if (model.solve()) {
            modelObj = model.getObjValue();
            System.out.println("模型目标值：" + model.getObjValue());
            System.out.println("模型变量值：");
            for (int i = 0; i < data.variableNumber; i++) {
                xValue[i] = model.getValue(x[i]);
                System.out.print(model.getValue(x[i]) + "\t");
            }
            System.out.println();
        } else {
            System.out.println("模型不可解");
        }
    }

    private void modelCopyNode(Node node) {
        node.nodeObj = modelObj;
        node.nodeResult = xValue.clone();
    }

    private void branchAndBoundMethod() throws IloException {
        curBest = 0;
        int n = 1;

        this.Nd = new Node[20];
        buildModel();
        solveModel();
        node1 = new Node(data);
        modelCopyNode(node1);
        queue.add(node1);
        Nd[0] = node1;
        System.out.println(Nd[0].nodeObj);
        tree.setRoot(Nd[0]);
        int count = 1;
        while (!queue.isEmpty()) {
            System.out.println("++++++++++++++++++++++++++++++++++++");
            System.out.println("第" + count + "次分支过程");
            Node node = queue.poll();
            node.LAncSpec = new int[10][10];
            node.RAncSpec = new int[10][10];
            System.out.println("选定模型：" + node.nodeObj);
            System.out.println("选定模型方案取值：");
            for (int i = 0; i < data.variableNumber; i++) {
                System.out.print(node.nodeResult[i] + "\t");
            }
            System.out.println();
            if (node.nodeObj < curBest) {
                System.out.println("队列长度：" + queue.size());
                count++;
                continue;
            } else {
                int idIndex = -1;
                for (int i = 0; i < node.nodeResult.length; i++) {
                    if (node.nodeResult[i] != (int) node.nodeResult[i]) {
                        idIndex = i;
                        break;
                    }
                }
                if (idIndex != -1) {
                    System.out.println("非整数变量：第" + (idIndex + 1) + "个变量的取值为" + node.nodeResult[idIndex]);
                }
                if (idIndex == -1) {
                    System.out.println("获得整数解");
                    if (curBest <= node.nodeObj) {
                        curBestNode = node;
                        curBest = curBestNode.nodeObj;
                    }
                } else {
                    ModelData a = node.data;
                    node1 = chooseBranch(node, idIndex, true);
                    if (node1 != null && node1.nodeObj > curBest) queue.add(node1);
                    node1.SourceDir = 0;
                    Nd[n] = node1;
                    tree.findNode(node, a).setLeftChild(Nd[n]);
                    Nd[n].SourceVar = idIndex;
                    Nd[n].father = tree.findNode(node, a);
                    n++;

                    node2 = chooseBranch(node, idIndex, false);
                    if (node2 != null && node2.nodeObj > curBest) queue.add(node2);
                    node2.SourceDir = 1;
                    Nd[n] = node2;
                    tree.findNode(node, a).setRightChild(Nd[n]);
                    Nd[n].SourceVar = idIndex;
                    Nd[n].father = tree.findNode(node, a);
                    n++;
                }
            }
            System.out.println("队列长度：" + queue.size());
            count++;
        }
        System.out.println("模型结果：");
        System.out.println(curBestNode.nodeObj);
        for (int i = 0; i < data.variableNumber; i++) {
            System.out.print(curBestNode.nodeResult[i] + "\t");
        }
    }

    private Node chooseBranch(Node node, int idIndex, boolean leftOrRight) throws IloException {
        Node newNode = new Node(data);
        newNode = node.nodeCopy();
        setVarsBound(node, idIndex, leftOrRight);
        if (model.solve()) {
            System.out.println(model);
            solveModel();
            modelCopyNode(newNode);
        } else {
            System.out.println("模型不可解");
            newNode.nodeObj = 0;
        }
        return newNode;
    }

    private void setVarsBound(Node node, int idIndex, boolean leftOrRight) throws IloException {
        //为新生成的节点设定正确的约束范围
        for (int i = 0; i < 10; i++) {
            for (int j = 0; j < 10; j++) {
                node.LAncSpec[i][j] = Integer.MAX_VALUE; // 1000
            }
        }
        ModelData a = node.data;
        Node y = node;
        int h = Integer.MAX_VALUE; // 1000
        int k = 0;
        int LVarNum[] = new int[20];
        int RVarNum[] = new int[20];
        tree.findNode(node, a).LSpc = (int) node.nodeResult[idIndex];
        tree.findNode(node, a).RSpc = (int) node.nodeResult[idIndex] + 1;
        if (node != Nd[0]) {
            while (node.father != null) {
                Node m = tree.findNode(node, a).father;
                if (tree.findNode(node, node.data).SourceDir == 0) {
                    tree.findNode(y, a).LAncSpec[tree.findNode(node, node.data).
                            SourceVar][LVarNum[tree.findNode(node, node.data).SourceVar]] =
                            tree.findNode(m, m.data).LSpc;
                    LVarNum[tree.findNode(node, node.data).SourceVar]++;
                } else if (tree.findNode(node, node.data).SourceDir == 1) {
                    tree.findNode(y, a).RAncSpec[tree.findNode(node, node.data).
                            SourceVar][RVarNum[tree.findNode(node, node.data).SourceVar]] =
                            tree.findNode(m, m.data).RSpc;
                    RVarNum[tree.findNode(node, node.data).SourceVar]++;
                }
                node = m;
            }
            h = findMinByFor(tree.findNode(node, a).LAncSpec[idIndex]);
            k = findMaxByFor(tree.findNode(node, a).RAncSpec[idIndex]);
            for (int i = 0; i < data.variableNumber; i++) {
                x[i].setLB(findMaxByFor(tree.findNode(y, a).RAncSpec[i]));
                x[i].setUB(findMinByFor(tree.findNode(y, a).LAncSpec[i]));
            }
        }
        if (leftOrRight) {
            for (int i = 0; i < y.nodeResult.length; i++) {
                if (i == idIndex) {
                    x[idIndex].setLB(0);
                    x[idIndex].setUB((int) y.nodeResult[idIndex] > h ? h : (int) y.nodeResult[idIndex]);
                }
            }
            System.out.println("非整数变量范围：" + 0 + "\t" + ((int) y.nodeResult[idIndex] > h ? h : (int) y.nodeResult[idIndex]));
            System.out.println("左支模型：");
            for (int i = 0; i < y.nodeResult.length; i++) {
                if (i == idIndex) {
                    System.out.println("变量" + (i + 1) + "：\t" + (0) + "\t" + ((int) y.nodeResult[idIndex]));
                }
            }
        } else {
            for (int i = 0; i < y.nodeResult.length; i++) {
                if (i == idIndex) {
                    x[idIndex].setLB(((int) y.nodeResult[idIndex] < k ? k : (int) y.nodeResult[idIndex]) + 1);
                    x[idIndex].setUB(Double.MAX_VALUE);
                }
            }
            System.out.println("非整数变量范围：" + ((int) y.nodeResult[idIndex] + 1) + "\t" + Double.MAX_VALUE);
            System.out.println("右支模型：");
            for (int i = 0; i < y.nodeResult.length; i++) {
                if (i == idIndex) {
                    System.out.println("变量" + (i + 1) + "：\t" + ((int) y.nodeResult[idIndex] + 1) + "\t" + (Double.MAX_VALUE));
                }
            }
        }


    }
    public static void main (String[]args) throws IloException {
        ModelData data = new ModelData();
        BranchAndBoundDemo lp = new BranchAndBoundDemo(data);
        lp.branchAndBoundMethod();
    }
    private static int findMaxByFor(int[] arr){
        int max = 0;
        for (int item : arr) {
            if (item > max) {
                max = item;
            }
        }
        return max;
    }//寻找数组中的最大值
    private static int findMinByFor(int[] arr){
        int min = arr[0];
        for (int i = 1; i < arr.length; i++) {
            if (min > arr[i]) {
                min = arr[i];
            }
        }
        return min;
    }//寻找数组中的最小值
}