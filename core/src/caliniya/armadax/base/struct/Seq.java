package caliniya.armadax.base.struct;

import caliniya.armadax.base.math.*;
import com.badlogic.gdx.ai.btree.decorator.Random;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.RandomXS128;
import com.badlogic.gdx.utils.*;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

@SuppressWarnings("unchecked")
public class Seq<T> implements Iterable<T> {
    /** 调试变量，统计分配的迭代器数量 */
    public static int iteratorsAllocated = 0;
    
    public int size;
    
    public T[] items;
    
    /** 是否有序 */
    public boolean ordered;
    
    private SeqIterable<T> iterable;

    /** 创建有序数组，容量16 */
    public Seq() {
        this(true, 16);
    }

    /** 创建有序数组，指定容量 */
    public Seq(int capacity) {
        this(true, capacity);
    }

    /** 创建有序/无序数组，容量16 */
    public Seq(boolean ordered) {
        this(ordered, 16);
    }

    /** 创建有序/无序数组，指定容量 */
    public Seq(boolean ordered, int capacity) {
        this.ordered = ordered;
        this.items = (T[])new Object[capacity];
    }

    /** TODO 这是复制过来的，需要理解 */
    public Seq(boolean ordered, int capacity, Class<?> arrayType) {
        this.ordered = ordered;
        this.items = (T[])java.lang.reflect.Array.newInstance(arrayType, capacity);
    }

    /** 从数组创建有序 */
    public Seq(T[] array) {
        this(true, array, 0, array.length);
    }
    
    
    //创建一个容量等于参数数组元素量的数组
    public Seq(Seq<? extends T> array){
        this(array.ordered, array.size, array.items.getClass().getComponentType());
        size = array.size;
        System.arraycopy(array.items, 0, items, 0, size);
    }

    /** 从数组创建，指定范围 */
    public Seq(boolean ordered, T[] array, int start, int count){
        this(ordered, count, array.getClass().getComponentType());
        size = count;
        System.arraycopy(array, start, items, 0, size);
    }

    /** 静态工厂方法 */
    public static <T> Seq<T> with(T... array) {
        return new Seq<>(array);
    }

    public static <T> Seq<T> with(Iterable<T> iterable) {
        Seq<T> out = new Seq<>();
        for (T item : iterable) {
            out.add(item);
        }
        return out;
    }

    /** 获取元素数量 */
    public int size() {
        return size;
    }

    //空的？
    public boolean isEmpty() {
        return size == 0;
    }

    //有没有东西
    public boolean any() {
        return size > 0;
    }

    /** 添加元素 */
    public Seq<T> add(T value){
        T[] items = this.items;
        if(size == items.length) items = resize(Math.max(8, (int)(size * 1.75f)));
        items[size++] = value;
        return this;
    }

    public Seq<T> add(T value1, T value2){
        T[] items = this.items;
        if(size + 1 >= items.length) items = resize(Math.max(8, (int)(size * 1.75f)));
        items[size] = value1;
        items[size + 1] = value2;
        size += 2;
        return this;
    }

    public Seq<T> add(T value1, T value2, T value3){
        T[] items = this.items;
        if(size + 2 >= items.length) items = resize(Math.max(8, (int)(size * 1.75f)));
        items[size] = value1;
        items[size + 1] = value2;
        items[size + 2] = value3;
        size += 3;
        return this;
    }

    public Seq<T> add(T value1, T value2, T value3, T value4){
        T[] items = this.items;
        if(size + 3 >= items.length) items = resize(Math.max(8, (int)(size * 1.8f))); // 1.75 isn't enough when size=5.
        items[size] = value1;
        items[size + 1] = value2;
        items[size + 2] = value3;
        items[size + 3] = value4;
        size += 4;
        return this;
    }

    public Seq<T> add(Seq<? extends T> array){
        addAll(array.items, 0, array.size);
        return this;
    }

    public Seq<T> add(T[] array){
        addAll(array, 0, array.length);
        return this;
    }

    public Seq<T> addAll(Seq<? extends T> array){
        addAll(array.items, 0, array.size);
        return this;
    }

    public Seq<T> addAll(Seq<? extends T> array, int start, int count){
        if(start + count > array.size)
            throw new IllegalArgumentException("start + count must be <= size: " + start + " + " + count + " <= " + array.size);
        addAll(array.items, start, count);
        return this;
    }

    public Seq<T> addAll(T... array){
        addAll(array, 0, array.length);
        return this;
    }

    public Seq<T> addAll(T[] array, int start, int count){
        T[] items = this.items;
        int sizeNeeded = size + count;
        if(sizeNeeded > items.length) items = resize(Math.max(8, (int)(sizeNeeded * 1.75f)));
        System.arraycopy(array, start, items, size, count);
        size += count;
        return this;
    }

    public Seq<T> addAll(Iterable<? extends T> items){
        if(items instanceof Seq){
            addAll((Seq)items);
        }else{
            for(T t : items){
                add(t);
            }
        }
        return this;
    }
    /** 获取元素 */
    public T get(int index){
        if(index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
        return items[index];
    }

    public T first(){
        if(size == 0) throw new IllegalStateException("Array is empty.");
        return items[0];
    }

    /** Returns the first item, or null if this Seq is empty. */
    public T firstOpt(){
        if(size == 0) return null;
        return items[0];
    }

    public T peek(){
        if(size == 0) throw new IllegalStateException("Array is empty.");
        return items[size - 1];
    }

    /** Removes and returns the last item. */
    public T pop(){
        if(size == 0) throw new IllegalStateException("Array is empty.");
        --size;
        T item = items[size];
        items[size] = null;
        return item;
    }

    /** 设置元素 */
    public void set(int index, T value){
        if(index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
        items[index] = value;
    }

    /** 插入元素 */
    public void insert(int index, T value){
        if(index > size) throw new IndexOutOfBoundsException("index can't be > size: " + index + " > " + size);
        T[] items = this.items;
        if(size == items.length) items = resize(Math.max(8, (int)(size * 1.75f)));
        if(ordered)
            System.arraycopy(items, index, items, index + 1, size - index);
        else
            items[size] = items[index];
        size++;
        items[index] = value;
    }

    /** 交换元素 */
    public void swap(int first, int second){
        if(first >= size) throw new IndexOutOfBoundsException("first can't be >= size: " + first + " >= " + size);
        if(second >= size) throw new IndexOutOfBoundsException("second can't be >= size: " + second + " >= " + size);
        T[] items = this.items;
        T firstValue = items[first];
        items[first] = items[second];
        items[second] = firstValue;
    }

    /** 包含检查 */
    public boolean containsAll(Seq<T> seq){
        return containsAll(seq, false);
    }

    /** @return whether this sequence contains every other element in the other sequence. */
    public boolean containsAll(Seq<T> seq, boolean identity){
        T[] others = seq.items;

        for(int i = 0; i < seq.size; i++){
            if(!contains(others[i], identity)){
                return false;
            }
        }
        return true;
    }

    public boolean contains(T value){
        return contains(value, false);
    }

    /**
     * Returns if this array contains value.
     * @param value May be null.
     * @param identity If true, == comparison will be used. If false, .equals() comparison will be used.
     * @return true if array contains value, false if it doesn't
     */
    public boolean contains(T value, boolean identity){
        T[] items = this.items;
        int i = size - 1;
        if(identity || value == null){
            while(i >= 0)
                if(items[i--] == value) return true;
        }else{
            while(i >= 0)
                if(value.equals(items[i--])) return true;
        }
        return false;
    }

    /** 索引查找 */
    public int indexOf(T value){
        return indexOf(value, false);
    }

    /**
     * Returns the index of first occurrence of value in the array, or -1 if no such value exists.
     * @param value May be null.
     * @param identity If true, == comparison will be used. If false, .equals() comparison will be used.
     * @return An index of first occurrence of value in array or -1 if no such value exists
     */
    public int indexOf(T value, boolean identity){
        T[] items = this.items;
        if(identity || value == null){
            for(int i = 0, n = size; i < n; i++)
                if(items[i] == value) return i;
        }else{
            for(int i = 0, n = size; i < n; i++)
                if(value.equals(items[i])) return i;
        }
        return -1;
    }

    public int indexOf(Boolf<T> value){
        T[] items = this.items;
        for(int i = 0, n = size; i < n; i++)
            if(value.get(items[i])) return i;
        return -1;
    }

    /**
     * Returns an index of last occurrence of value in array or -1 if no such value exists. Search is started from the end of an
     * array.
     * @param value May be null.
     * @param identity If true, == comparison will be used. If false, .equals() comparison will be used.
     * @return An index of last occurrence of value in array or -1 if no such value exists
     */
    public int lastIndexOf(T value, boolean identity){
        T[] items = this.items;
        if(identity || value == null){
            for(int i = size - 1; i >= 0; i--)
                if(items[i] == value) return i;
        }else{
            for(int i = size - 1; i >= 0; i--)
                if(value.equals(items[i])) return i;
        }
        return -1;
    }

    /** 移除元素 */
    public boolean remove(T value){
        return remove(value, false);
    }

    /** Removes a single value by predicate.
     * @return whether the item was found and removed. */
    public boolean remove(Boolf<T> value){
        for(int i = 0; i < size; i++){
            if(value.get(items[i])){
                remove(i);
                return true;
            }
        }
        return false;
    }

    /**
     * Removes the first instance of the specified value in the array.
     * @param value May be null.
     * @param identity If true, == comparison will be used. If false, .equals() comparison will be used.
     * @return true if value was found and removed, false otherwise
     */
    public boolean remove(T value, boolean identity){
        T[] items = this.items;
        if(identity || value == null){
            for(int i = 0, n = size; i < n; i++){
                if(items[i] == value){
                    remove(i);
                    return true;
                }
            }
        }else{
            for(int i = 0, n = size; i < n; i++){
                if(value.equals(items[i])){
                    remove(i);
                    return true;
                }
            }
        }
        return false;
    }

    /** Removes and returns the item at the specified index. */
    public T remove(int index){
        if(index >= size) throw new IndexOutOfBoundsException("index can't be >= size: " + index + " >= " + size);
        T[] items = this.items;
        T value = items[index];
        size--;
        if(ordered)
            System.arraycopy(items, index + 1, items, index, size - index);
        else
            items[index] = items[size];
        items[size] = null;
        return value;
    }

    /** Removes the items between the specified indices, inclusive. */
    public void removeRange(int start, int end){
        if(end >= size) throw new IndexOutOfBoundsException("end can't be >= size: " + end + " >= " + size);
        if(start > end) throw new IndexOutOfBoundsException("start can't be > end: " + start + " > " + end);
        T[] items = this.items;
        int count = end - start + 1;
        if(ordered)
            System.arraycopy(items, start + count, items, start, size - (start + count));
        else{
            int lastIndex = this.size - 1;
            for(int i = 0; i < count; i++)
                items[start + i] = items[lastIndex - i];
        }
        size -= count;
    }

    /** @return this object */
    public Seq<T> removeAll(Boolf<T> pred){
        Iterator<T> iter = iterator();
        while(iter.hasNext()){
            if(pred.get(iter.next())){
                iter.remove();
            }
        }
        return this;
    }

    public boolean removeAll(Seq<? extends T> array){
        return removeAll(array, false);
    }

    /**
     * Removes from this array all of elements contained in the specified array.
     * @param identity True to use ==, false to use .equals().
     * @return true if this array was modified.
     */
    public boolean removeAll(Seq<? extends T> array, boolean identity){
        int size = this.size;
        int startSize = size;
        T[] items = this.items;
        if(identity){
            for(int i = 0, n = array.size; i < n; i++){
                T item = array.get(i);
                for(int ii = 0; ii < size; ii++){
                    if(item == items[ii]){
                        remove(ii);
                        size--;
                        break;
                    }
                }
            }
        }else{
            for(int i = 0, n = array.size; i < n; i++){
                T item = array.get(i);
                for(int ii = 0; ii < size; ii++){
                    if(item.equals(items[ii])){
                        remove(ii);
                        size--;
                        break;
                    }
                }
            }
        }
        return size != startSize;
    }

    /** 清空 */
    public Seq<T> clear(){
        T[] items = this.items;
        for(int i = 0, n = size; i < n; i++)
            items[i] = null;
        size = 0;
        return this;
    }
    /** 确保容量 */
    public T[] ensureCapacity(int additionalCapacity){
        if(additionalCapacity < 0)
            throw new IllegalArgumentException("additionalCapacity must be >= 0: " + additionalCapacity);
        int sizeNeeded = size + additionalCapacity;
        if(sizeNeeded > items.length) resize(Math.max(8, sizeNeeded));
        return items;
    }

    /** 缩容 */
    public T[] shrink(){
        if(items.length != size) resize(size);
        return items;
    }

    /** 设置大小 */
    protected T[] resize(int newSize){
        T[] items = this.items;
        T[] newItems = (T[])(items.getClass() == Object[].class ? new Object[newSize] : java.lang.reflect.Array.newInstance(items.getClass().getComponentType(), newSize));
        System.arraycopy(items, 0, newItems, 0, Math.min(size, newItems.length));
        this.items = newItems;
        return newItems;
    }

    /** 排序 */
    public Seq<T> sort(){
        Sort.instance().sort(items, 0, size);
        return this;
    }

    /** Sorts the array. This method is not thread safe (uses {@link Sort#instance()}). */
    public Seq<T> sort(Comparator<? super T> comparator){
        Sort.instance().sort(items, comparator, 0, size);
        return this;
    }

    public Seq<T> sort(Floatf<? super T> comparator){
        Sort.instance().sort(items, (c1, c2) -> Float.compare(comparator.get(c1), comparator.get(c2)), 0, size);
        return this;
    }

    /** 反转 */
    public Seq<T> reverse(){
        T[] items = this.items;
        for(int i = 0, lastIndex = size - 1, n = size / 2; i < n; i++){
            int ii = lastIndex - i;
            T temp = items[i];
            items[i] = items[ii];
            items[ii] = temp;
        }

        return this;
    }

    /** 洗牌 */
    public Seq<T> shuffle(){
        T[] items = this.items;
        for(int i = size - 1; i >= 0; i--){
            int ii = Mathf.random(i);
            T temp = items[i];
            items[i] = items[ii];
            items[ii] = temp;
        }

        return this;
    }

    /** 截断 */
    public void truncate(int newSize){
        if(newSize < 0) throw new IllegalArgumentException("newSize must be >= 0: " + newSize);
        if(size <= newSize) return;
        for(int i = newSize; i < size; i++)
            items[i] = null;
        size = newSize;
    }

    /** 随机元素 */
    public T random(Rand rand){
        if(size == 0) return null;
        return items[rand.random(0, size - 1)];
    }

    /** Returns a random item from the array, or null if the array is empty.返回数组中的随机项，如果数组为空，则返回NULL。 */
    public T random(){
        return random(Mathf.rand);
    }

    /** Returns a random item from the array, excluding the specified element. If the array is empty, returns null.
     * If this array only has one element, returns that element.返回数组中的随机项，不包括指定的元素。如果数组为空，则返回NULL。\n*如果此数组只有一个元素，则返回该元素。 */
    public T random(T exclude){
        if(exclude == null) return random();
        if(size == 0) return null;
        if(size == 1) return first();

        int eidx = indexOf(exclude);
        //this item isn't even in the array!
        if(eidx == -1) return random();

        //shift up the index
        int index = Mathf.random(0, size - 2);
        if(index >= eidx){
            index ++;
        }
        return items[index];
    }

    /** 转换为数组 */
    public T[] toArray(){
        return toArray(items.getClass().getComponentType());
    }

    public <V> V[] toArray(Class type){
        V[] result = (V[])java.lang.reflect.Array.newInstance(type, size);
        System.arraycopy(items, 0, result, 0, size);
        return result;
    }
    
    /** 遍历 */
    public <E extends T> void each(Boolf<? super T> pred, Cons<E> consumer){
        for(int i = 0; i < size; i++){
            if(pred.get(items[i])) consumer.get((E)items[i]);
        }
    }

    public void each(Cons<? super T> consumer){
        for(int i = 0; i < size; i++){
            consumer.get(items[i]);
        }
    }

    /**  映射 */
    public <R> Seq<R> map(Func<T, R> mapper){
        Seq<R> arr = new Seq<>(size);
        for(int i = 0; i < size; i++){
            arr.add(mapper.get(items[i]));
        }
        return arr;
    }

    /** 函数式操作 - 过滤 */
    public Seq<T> select(Boolf<T> predicate){
        Seq<T> arr = new Seq<>();
        for(int i = 0; i < size; i++){
            if(predicate.get(items[i])){
                arr.add(items[i]);
            }
        }
        return arr;
    }

    /** 函数式操作 - 移除所有不匹配的 */
    public Seq<T> retainAll(Boolf<T> predicate){
        return removeAll(e -> !predicate.get(e));
    }

    public int count(Boolf<T> predicate){
        int count = 0;
        for(int i = 0; i < size; i++){
            if(predicate.get(items[i])){
                count ++;
            }
        }
        return count;
    }

    /** 查找 */
    public T find(Boolf<T> predicate){
        for(int i = 0; i < size; i++){
            if(predicate.get(items[i])){
                return items[i];
            }
        }
        return null;
    }

    /** 所有匹配 */
    public boolean allMatch(Boolf<T> predicate){
        for(int i = 0; i < size; i++){
            if(!predicate.get(items[i])){
                return false;
            }
        }
        return true;
    }

    /** 函数式操作 - 任意匹配 
    public boolean anyMatch(Boolf<T> predicate) {
        for (int i = 0; i < size; i++) {
            if (predicate.get(array.get(i))) {
                return true;
            }
        }
        return false;
    }*/

    /** 转换为集合 */
    public ObjectSet<T> asSet(){
    ObjectSet<T> set = new ObjectSet<>(size);
    for(int i = 0; i < size; i++){
        set.add(items[i]);
    }
    return set;
}

   
    /** 转换为字符串 */
    @Override
    public String toString(){
        if(size == 0) return "[]";
        T[] items = this.items;
        java.lang.StringBuilder buffer = new java.lang.StringBuilder(32);
        buffer.append('[');
        buffer.append(items[0]);
        for(int i = 1; i < size; i++){
            buffer.append(", ");
            buffer.append(items[i]);
        }
        buffer.append(']');
        return buffer.toString();
    }

    public String toString(String separator, Func<T, String> stringifier){
        if(size == 0) return "";
        T[] items = this.items;
        java.lang.StringBuilder buffer = new java.lang.StringBuilder(32);
        buffer.append(stringifier.get(items[0]));
        for(int i = 1; i < size; i++){
            buffer.append(separator);
            buffer.append(stringifier.get(items[i]));
        }
        return buffer.toString();
    }

    public String toString(String separator){
        return toString(separator, String::valueOf);
    }


    /** 迭代器 */
    @Override
    public Iterator<T> iterator() {
        if (iterable == null) iterable = new SeqIterable<>(this);
        return iterable.iterator();
    }

    /** 内部迭代器类 */
    private static class SeqIterable<T> implements Iterable<T> {
        final Seq<T> array;
        final boolean allowRemove;
        private SeqIterator iterator1 = new SeqIterator(), iterator2 = new SeqIterator();


        public SeqIterable(Seq<T> array){
            this(array, true);
        }

        public SeqIterable(Seq<T> array, boolean allowRemove){
            this.array = array;
            this.allowRemove = allowRemove;
        }
        
        @Override
        public Iterator<T> iterator() {
            if (iterator1.done) {
                iterator1.index = 0;
                iterator1.done = false;
                return iterator1;
            }

            if (iterator2.done) {
                iterator2.index = 0;
                iterator2.done = false;
                return iterator2;
            }
            
            return new SeqIterator();
        }

        private class SeqIterator implements Iterator<T> {
            int index;
            boolean done = true;

            {
                iteratorsAllocated++;
            }

            @Override
            public boolean hasNext() {
                if (index >= array.size) done = true;
                return index < array.size;
            }

            @Override
            public T next() {
                if (index >= array.size) throw new NoSuchElementException(String.valueOf(index));
                return array.get(index++);
            }

            @Override
            public void remove(){
                if(!allowRemove) throw new RuntimeException("Remove not allowed.");
                index--;
                array.remove(index);
            }
        }
    }

    /** 函数式接口定义*/
    public interface Cons<T> {
        void get(T t);
    }

    public interface Boolf<T> {
        boolean get(T t);
    }

    public interface Func<T, R> {
        R get(T t);
    }

    public interface Floatf<T> {
        float get(T t);
    }

    public interface Intf<T> {
        int get(T t);
    }
}