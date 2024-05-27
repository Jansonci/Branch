import ilog.concert.IloException;
import ilog.concert.IloNumExpr;
import ilog.concert.IloNumVar;
import ilog.concert.IloNumVarType;
import ilog.cplex.IloCplex;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.PriorityQueue;

//    数据参数定义
class Data{
    double[] objectiveCoefficient={0,-2,0,-2,-1,0,-1,-1};
    //约束系数
    double[][] constraintCoefficient={{0,-4,-2,-4,0,0,0,-1},{0,-4,-2,0,-3,-2,0,0},{-2,0,-2,-4,0,-2,-1,0},{-2,-4,0,-4,0,0,0,-1},{1,1,1,1,1,1,1,1}};
    // {70,50,35,0,0,0,0,0},
    //约束值
    double[] constraintValue={-6,-9,-5,-7,8};
    //变量数量
    int variableNumber=8;
    //约束数量
    int constrainNumber=5;
}
//使用cplex求解整数规划
public class IntegerProgrammingDemo {
    //    定义数据
    Data data;

    public IntegerProgrammingDemo(Data data) {
        this.data = data;
    }

    //    定义cplex内部对象
    IloCplex model;
    //    定义变量
    public IloNumVar[] x;

    //    求解函数
    public void solve() throws IloException {
        if (model.solve() == false) {
//            模型不可解
            System.out.println("模型不可解");
            return;
        } else {
            System.out.println("目标值：" + model.getObjValue());
            for (int i = 0; i < data.variableNumber; i++) {
                System.out.println("变量值x[" + (i + 1) + "]:" + model.getValue(x[i]));
            }
        }
    }

    //    根据数学模型建立求解模型
    public void BuildModel() throws IloException {
//        model
        model = new IloCplex();
        model.setOut(null);
//        variables
        x = new IloNumVar[data.variableNumber];
//        定义cplex变量x的数据类型及取值范围
        for (int i = 0; i < data.variableNumber; i++) {
            x[i] = model.numVar(0, 1e15, IloNumVarType.Int, "x[" + i + "]");
        }
//        设置目标函数
        IloNumExpr obj = model.numExpr();
        for (int i = 0; i < data.variableNumber; i++) {
            obj = model.sum(obj, model.prod(data.objectiveCoefficient[i], x[i]));
        }
        model.addMaximize(obj);
//        添加约束
        for (int k = 0; k < data.constrainNumber; k++) {
            IloNumExpr expr = model.numExpr();
            for (int i = 0; i < data.variableNumber; i++) {
                expr = model.sum(expr, model.prod(data.constraintCoefficient[k][i], x[i]));
            }
            model.addLe(expr, data.constraintValue[k]);
        }
        for (int i = 0; i < data.variableNumber; i++) {
            IloNumExpr expr = model.numExpr();
            expr = model.prod(1, x[i]);
            model.addLe(expr, 1);
            model.addGe(expr, 0);
        }
    }

        public static void main (String[]args) throws IloException {
            Data data = new Data();
            IntegerProgrammingDemo lp = new IntegerProgrammingDemo(data);
            lp.BuildModel();
            lp.solve();

        }
    }


