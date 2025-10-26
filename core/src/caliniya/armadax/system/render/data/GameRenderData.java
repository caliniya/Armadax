package caliniya.armadax.system.render.data;

import caliniya.armadax.base.struct.Seq;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.Color;

public class GameRenderData {
    public static final GameRenderData instance = new GameRenderData();
    
    // 使用数组存储所有渲染数据
    public final Seq<TextureRegion> regions = new Seq<>(1024);
    public final Seq<Float> positionsX = new Seq<>(1024);
    public final Seq<Float> positionsY = new Seq<>(1024);
    public final Seq<Float> widths = new Seq<>(1024);
    public final Seq<Float> heights = new Seq<>(1024);
    public final Seq<Float> originsX = new Seq<>(1024);
    public final Seq<Float> originsY = new Seq<>(1024);
    public final Seq<Float> scalesX = new Seq<>(1024);
    public final Seq<Float> scalesY = new Seq<>(1024);
    public final Seq<Float> rotations = new Seq<>(1024);
    public final Seq<Color> colors = new Seq<>(1024);
    
    // 当前渲染项数量
    private int count = 0; // 改为0，从0开始
    // 最大容量
    private int capacity = 1024;
    
    private GameRenderData() {
        // 移除 ensureCapacity(capacity)，因为集合已经初始化了容量
    }
    
    /**
     * 添加一个渲染项
     */
    public void add(TextureRegion region, float x, float y, float width, float height) {
        ensureCapacity(count + 1);
        
        // 使用 add() 而不是 set()
        regions.add(region);
        positionsX.add(x);
        positionsY.add(y);
        widths.add(width);
        heights.add(height);
        originsX.add(width / 2f); // 默认原点在中心
        originsY.add(height / 2f);
        scalesX.add(1f);
        scalesY.add(1f);
        rotations.add(0f);
        colors.add(Color.WHITE);
        
        count++;
    }
    
    /**
     * 添加一个完整的渲染项
     */
    public void add(TextureRegion region, float x, float y, float width, float height,
                   float originX, float originY, float scaleX, float scaleY, 
                   float rotation, Color color) {
        ensureCapacity(count + 1);
        
        // 使用 add() 而不是 set()
        regions.add(region);
        positionsX.add(x);
        positionsY.add(y);
        widths.add(width);
        heights.add(height);
        originsX.add(originX);
        originsY.add(originY);
        scalesX.add(scaleX);
        scalesY.add(scaleY);
        rotations.add(rotation);
        colors.add(color);
        
        count++;
    }
    
    /**
     * 确保容量足够
     */
    private void ensureCapacity(int newCapacity) {
        if (newCapacity > capacity) {
            capacity = Math.max(capacity * 2, newCapacity);
            
            regions.ensureCapacity(capacity);
            positionsX.ensureCapacity(capacity);
            positionsY.ensureCapacity(capacity);
            widths.ensureCapacity(capacity);
            heights.ensureCapacity(capacity);
            originsX.ensureCapacity(capacity);
            originsY.ensureCapacity(capacity);
            scalesX.ensureCapacity(capacity);
            scalesY.ensureCapacity(capacity);
            rotations.ensureCapacity(capacity);
            colors.ensureCapacity(capacity);
        }
    }
    
    /**
     * 清空所有渲染数据
     */
    public void clear() {
        count = 0;
        // 清空所有集合
        regions.clear();
        positionsX.clear();
        positionsY.clear();
        widths.clear();
        heights.clear();
        originsX.clear();
        originsY.clear();
        scalesX.clear();
        scalesY.clear();
        rotations.clear();
        colors.clear();
    }
    
    /**
     * 获取当前渲染项数量
     */
    public int getCount() {
        return count;
    }
    
    /**
     * 获取指定索引的渲染数据
     */
    public TextureRegion getRegion(int index) {
        return regions.get(index);
    }
    
    public float getX(int index) {
        return positionsX.get(index);
    }
    
    public float getY(int index) {
        return positionsY.get(index);
    }
    
    public float getWidth(int index) {
        return widths.get(index);
    }
    
    public float getHeight(int index) {
        return heights.get(index);
    }
    
    public float getOriginX(int index) {
        return originsX.get(index);
    }
    
    public float getOriginY(int index) {
        return originsY.get(index);
    }
    
    public float getScaleX(int index) {
        return scalesX.get(index);
    }
    
    public float getScaleY(int index) {
        return scalesY.get(index);
    }
    
    public float getRotation(int index) {
        return rotations.get(index);
    }
    
    public Color getColor(int index) {
        return colors.get(index);
    }
}