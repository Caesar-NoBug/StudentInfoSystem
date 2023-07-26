package main.java.com.caesar.utils;

import java.util.ArrayList;
import java.util.List;

//树状数组(此为特化版本，数组每一位的值是0或1<表示该节点是否存在>,用于在O（(logn)^2）时间内求第k个数)
public class IndexedTree {

    private List<Integer> array;

    public IndexedTree(int size) {
        this.array = new ArrayList<>(size + 1);
        array.add(0);
        for (int i = 1; i <= size; i++) {
            array.add(lowbit(i));
        }
    }

    //添加新节点
    private void expand(){
        int i = array.size();
        array.add((ask(i - 1) - ask(i - lowbit(i)))+ 1);
    }

    //将节点标记为存在或不存在
    public void change(int x, boolean isExist){
        if(x == array.size()){
            if(isExist) expand();
            return;
        }
        int change = isExist ? 1 : -1;
        for (int i = x; i < array.size(); i += lowbit(i)) {
            array.set(i, array.get(i) + change);
        }
    }

    //返回第x个节点
    public int indexAt(int x){
        int l = 1, r = array.size() - 1;

        while (l < r){
            int mid = l + r >> 1;
            if(ask(mid) >= x) r = mid;
            else l = mid + 1;
        }

        return ask(l) >= x ? l : -1;
    }

    //求1~x的前缀和
    private int ask(int x){
        int sum = 0;
        for (int i = x; i > 0; i -= lowbit(i)) {
            sum += array.get(i);
        }
        return sum;
    }

    //lowbit运算
    private int lowbit(int x){
        return x & -x;
    }
}
