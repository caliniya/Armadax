package caliniya.armadax.world;

import caliniya.armadax.world.*;

public class World {
    public int width, height;
    public Tile[][] tiles;
    
    public World(int width, int height) {
        this.width = width;
        this.height = height;
        this.tiles = new Tile[width][height];
        generateWorld();
    }
    
    private void generateWorld() {
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                tiles[x][y] = new Tile(x, y);
                if (Math.random() < 0.1f) {
                    
                } else if (Math.random() < 0.05f) {
                    
                }
            }
        }
    }
    
    public Tile getTile(int x, int y) {
        if (x < 0 || x >= width || y < 0 || y >= height) return null;
        return tiles[x][y];
    }
}