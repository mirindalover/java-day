### 排序

常用排序算法:冒泡排序、快排


| 排序算法    |  时间复杂度   |   最好  |   最坏  |   空间复杂度  |  稳定性   |
| --- | --- | --- | --- | --- | --- |
|  冒泡排序   |  O(n2)   |  O(n)   |  O(n2)   |   O(1)  |  稳定   |
|  快速排序   |     |     |     |     |     |
|     |     |     |     |     |     |
|     |     |     |     |     |     |


#### 冒泡排序


逐个比较相邻的元素，如果前>后，交换

一次循环确定了最后一个是最大值

设置flag标示没有任何元素交换。可以提前推出循环

```java
 public int[] sort(int[] sourceArray) throws Exception {
    // 对 arr 进行拷贝，不改变参数内容
    int[] arr = Arrays.copyOf(sourceArray, sourceArray.length);

    for (int i = 1; i < arr.length; i++) {
        // 设定一个标记，若为true，则表示此次循环没有进行交换，也就是待排序列已经有序，排序已经完成。
        boolean flag = true;

        for (int j = 0; j < arr.length - i; j++) {
            if (arr[j] > arr[j + 1]) {
                int tmp = arr[j];
                arr[j] = arr[j + 1];
                arr[j + 1] = tmp;

                flag = false;
            }
        }

        if (flag) {
            break;
        }
    }
    return arr;
}
```

#### 快排


采用分治的思想

选择一个基准数，把小于的移动到基准的左，大于的移动到右

```java
public class QuickSort {
    public static void quickSort(int[] arr, int left, int right) {
        if (left >= right) {
            return;
        }
        int pivot = partition(arr, left, right);
        quickSort(arr, left, pivot - 1);
        quickSort(arr, pivot + 1, right);
    }
    
    private static int partition(int[] arr, int left, int right) {
        int pivot = arr[left];
        int i = left + 1;
        int j = right;
        while (true) {
        	//交换时维护2个指针,减少交换操作
            while (i <= j && arr[i] < pivot) {
                i++;
            }
            while (i <= j && arr[j] > pivot) {
                j--;
            }
            if (i >= j) {
                break;
            }
            swap(arr, i, j);
            i++;
            j--;
        }
        //把基准放到对应的中间位置
        swap(arr, left, j);
        return j;
    }
    
    private static void swap(int[] arr, int i, int j) {
        int temp = arr[i];
        arr[i] = arr[j];
        arr[j] = temp;
    }
}
```


参考：

https://mp.weixin.qq.com/s/vn3KiV-ez79FmbZ36SX9lg