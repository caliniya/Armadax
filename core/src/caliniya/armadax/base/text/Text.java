package caliniya.armadax.base.text;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator;
import com.badlogic.gdx.graphics.g2d.freetype.FreeTypeFontGenerator.FreeTypeFontParameter;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.math.Matrix4;

public class Text {
    private BitmapFont font;
    private FreeTypeFontGenerator generator;
    private GlyphLayout glyphLayout; // 用于测量文本尺寸
    private static Text text;
    
    static {
        text = new Text();
    }
    
    private Text() {
        initializeFont("font/normal.ttf", 25); 
        glyphLayout = new GlyphLayout(); // 初始化GlyphLayout
    }

    private void initializeFont(String fontFilePath, int fontSize) {
        generator = new FreeTypeFontGenerator(Gdx.files.internal(fontFilePath));
        FreeTypeFontParameter parameter = createFontParameter(fontSize);
        font = generator.generateFont(parameter);
    }

    private FreeTypeFontParameter createFontParameter(int fontSize) {
        FreeTypeFontParameter parameter = new FreeTypeFontParameter();
        parameter.size = fontSize;
        parameter.incremental = true;
        return parameter;
    }

    public void dispose() {
        font.dispose();
        generator.dispose();
    }
    
    public static Text getInstance() {
        return text;
    }
    
    
    public void draw(String text, float x, float y , Batch batch) {
        font.draw(batch ,text, x, y);
    }
    
    public void draw(String text, float x, float y, Color color, Batch batch) {
        font.setColor(color);
        font.draw(batch, text, x, y);
        font.setColor(Color.WHITE);
    }
    
    /**
     * 获取文本宽度
     * @param text 要测量的文本
     * @return 文本宽度（像素）
     */
    public float getWidth(String text) {
        glyphLayout.setText(font, text);
        return glyphLayout.width;
    }
    
    /**
     * 获取文本高度
     * @param text 要测量的文本
     * @return 文本高度（像素）
     */
    public float getHeight() {
        return font.getLineHeight();
    }
    
    /**
     * 获取字体行高（不依赖具体文本）
     * @return 字体行高
     */
    public float getLineHeight() {
        return font.getLineHeight();
    }
    
    
    
    public BitmapFont getFont() {
        return font;
    }
    
    /**
     * 设置投影矩阵（用于调整绘制坐标系）
     * @param combined 投影矩阵
     */
    public void setProjectionMatrix(Matrix4 combined) {
    }
}
