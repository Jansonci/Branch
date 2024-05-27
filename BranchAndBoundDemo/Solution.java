import java.sql.Array;
import java.util.*;

public class Solution {
    public List<Integer> findAnagrams(String s, String p) {
        List<Integer> list = new ArrayList<>();
        char[] charArray = s.toCharArray();
        char[] p1 = p.toCharArray();
        Arrays.sort(p1);
        for (int i = 0; i < (charArray.length - p.length()-1); i++) {
            char[] s1 = s.substring(i, i + p.length()).toCharArray();
            Arrays.sort(s1);
            if (s1 == p1) {
                list.add(i);
            }
        }
        return list;
    }

    public int subarraySum(int[] nums, int k) {
        int count = 0;
        Arrays.sort(nums);
        int m1 = 0;
        int m2 = 0;
        while (m2<k&&m2<nums.length){
            if(sum(Arrays.copyOfRange(nums, m1,m2))==k){
                count++;
                m2++;
            }
            else if(sum(Arrays.copyOfRange(nums, m1,m2))<k){
                m2++;
            }else m1++;
        }
        return count;
    }

    public int sum(int[] nums){
        int sum = 0;
        for (int i =0; i<nums.length; i++){
            sum = nums[i] + sum;
        }
        return sum;
    }

    public int maxSubArray(int[] nums) {
        int sum = 0;
        int max = Integer.MIN_VALUE;
        int min = 0;
        int maxmark =0;
        for (int i=0; i<nums.length; i++){
            if(sum+nums[i]>max) {
                max=sum+nums[i];
                maxmark=i;
            }
            sum=sum+nums[i];
        }
        int sum1 =0;
        for (int i=0; i<nums.length; i++){
            if((sum1+nums[i]<min)&&(i<maxmark)) {
                min=sum1+nums[i];
            }
            sum1=sum1+nums[i];
        }
        return max-min;
    }

    public int[][] merge(int[][] intervals) {
        int min=Integer.MAX_VALUE;
        int max=Integer.MIN_VALUE;
        int finalLength=intervals.length;
        Stack<int[]> stack = new Stack<>();
        for (int i=0; i<intervals.length; i++){
            if(intervals[i][0]<=max){
                min= Math.min(min,intervals[i][0]);
                max=Math.max(max,intervals[i][1]);
                stack.pop();
                stack.push(new int[]{min,max});
                finalLength--;
            }else {
                min = intervals[i][0];
                max = intervals[i][1];
                stack.push(new int[]{min, max});
            }
        }
        int[][] result=new int[finalLength][];
        for (int i=finalLength-1; i>=0; i--){
            result[i]=stack.pop();
        }
        return result;
    }
    
    public static void main(String[] args) {
       new Solution().trap(new int[]{0,1,0,2,1,0,1,3,2,1,2,1});
    }

//    public List<String> generateParenthesis(int n) {
////        Map<LinkedList<String>,Integer> map = new HashMap<>();
//        List<String> result=new ArrayList<>();
//        LinkedList<String> candidate = new LinkedList<>();
//        BinaryTree tree=new BinaryTree();
//        tree.
//        candidate.add("(");
////        map.put(candidate,1);
//        int expectation=1;
//        for(int i =0; i<2*n; i++){
//            candidate.add("(");
////            map.put(candidate,map.get(candidate.removeLast())+1);
//            expectation++;
//            if (expectation%2==1){
//                candidate.removeLast();
//                expectation--;
//            }
//
//            candidate.add(")");
//            expectation--;
//            if(expectation<0){
//                candidate.removeLast();
//                expectation++;
//            }
//        }
//    }
    public int[] dailyTemperatures(int[] temperatures){
        int[] result = new int[temperatures.length];
        result[temperatures.length-1]=0;
        Stack<Integer> stack= new Stack<>();
        stack.push(temperatures.length-1);
        for (int i = temperatures.length-2; i >= 0 ; i--) {
            if (temperatures[i]>=temperatures[stack.peek()]){
                while (!stack.isEmpty()&&temperatures[i]>=temperatures[stack.peek()]){
                    stack.pop();
                }
                if(stack.isEmpty()) {
                    result[i]=0;
                    stack.push(i);
                }
                else if(temperatures[i]<temperatures[stack.peek()]) {
                    result[i]=stack.peek()-i;
                    stack.push(i);
                }
            }
            else {
                stack.push(i);
                result[i]=1;
            }
        }
        return result;
    }

    public int trap(int[] height) {
        int Switch = 1;
        int tallest = height[0];
        int result = 0;
        Stack<Integer> stack=new Stack<>();
        stack.push(0);
        for (int i = 1; i < height.length; i++) {
            if (height[i]>=height[i-1]) {
                   if(Switch==1) {
                        stack.push(i);
                        tallest = height[i];
                    }
                    else {
                        while (height[stack.peek()] < height[i] && height[stack.peek()] < tallest) {
                            stack.pop();
                        }
                        stack.push(i);
                        if (height[i] < tallest) Switch = 0;
                        else {
                            Switch = 1;
                            tallest = height[i];
                        }
                    }
            }
            else {
                Switch=0;
                stack.push(i);
            }
        }
        System.out.println(stack);
        while (!stack.isEmpty()&&stack.size()>1){
            int a=stack.pop();
            int b=stack.peek();
            int shadow =0;
            for (int i=b+1; i<a; i++){
                shadow=shadow+height[i];
            }
            result = result+(a-b-1)*(Math.min(height[a],height[b]))-shadow;
        }
        System.out.println(result);
        return result;
    }
}


