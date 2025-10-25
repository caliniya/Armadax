package caliniya.armadax.base.math;

import caliniya.armadax.base.math.Rand;
import com.badlogic.gdx.math.Vector2;
import java.util.Arrays;

public final class Mathf {
    public static final int[] signs = {-1, 1};
    public static final int[] zeroOne = {0, 1};
    public static final boolean[] booleans = {true, false};
    public static final float FLOAT_ROUNDING_ERROR = 0.000001f;
    public static final float PI = (float) Math.PI;
    public static final float pi = PI;
    public static final float halfPi = PI / 2;
    public static final float PI2 = PI * 2;
    public static final float E = (float) Math.E;
    public static final float sqrt2 = (float) Math.sqrt(2);
    public static final float sqrt3 = (float) Math.sqrt(3);
    public static final float radiansToDegrees = 180f / PI;
    public static final float radDeg = radiansToDegrees;
    public static final float degreesToRadians = PI / 180;
    public static final float degRad = degreesToRadians;
    public static final double doubleDegRad = degreesToRadians;
    public static final double doubleRadDeg = radiansToDegrees;

    private static final int sinBits = 14;
    private static final int sinMask = ~(-1 << sinBits);
    private static final int sinCount = sinMask + 1;
    private static final float[] sinTable = new float[sinCount];
    private static final float radFull = PI * 2;
    private static final float degFull = 360;
    private static final float radToIndex = sinCount / radFull;
    private static final float degToIndex = sinCount / degFull;
    private static final int BIG_ENOUGH_INT = 16 * 1024;
    private static final double BIG_ENOUGH_FLOOR = BIG_ENOUGH_INT;
    private static final double CEIL = 0.9999999;
    private static final double BIG_ENOUGH_ROUND = BIG_ENOUGH_INT + 0.5f;
    private static final Rand seedr = new Rand();
    private static final Vector2 v1 = new Vector2();
    private static final Vector2 v2 = new Vector2();
    private static final Vector2 v3 = new Vector2();

    static {
        for (int i = 0; i < sinCount; i++) {
            sinTable[i] = (float) Math.sin((i + 0.5f) / sinCount * radFull);
        }
        
        for (int i = 0; i < 360; i += 90) {
            sinTable[(int) (i * degToIndex) & sinMask] = (float) Math.sin(i * degreesToRadians);
        }

        sinTable[0] = 0f;
        sinTable[(int) (90 * degToIndex) & sinMask] = 1f;
        sinTable[(int) (180 * degToIndex) & sinMask] = 0f;
        sinTable[(int) (270 * degToIndex) & sinMask] = -1f;
    }

    public static Rand rand = new Rand();

    // 三角函数
    public static float sin(float radians) {
        return sinTable[(int) (radians * radToIndex) & sinMask];
    }

    public static float cos(float radians) {
        return sinTable[(int) ((radians + PI / 2) * radToIndex) & sinMask];
    }

    public static float sinDeg(float degrees) {
        return sinTable[(int) (degrees * degToIndex) & sinMask];
    }

    public static float cosDeg(float degrees) {
        return sinTable[(int) ((degrees + 90) * degToIndex) & sinMask];
    }

    public static float absin(float scl, float mag) {
        return absin(0, scl, mag); // 需要Time类，这里简化处理
    }

    public static float absin(float in, float scl, float mag) {
        return (sin(in, scl * 2f, mag) + mag) / 2f;
    }

    public static float tan(float radians, float scl, float mag) {
        return (sin(radians / scl)) / (cos(radians / scl)) * mag;
    }

    public static float sin(float scl, float mag) {
        return sin(0 / scl) * mag; // 需要Time类
    }

    public static float sin(float radians, float scl, float mag) {
        return sin(radians / scl) * mag;
    }

    public static float cos(float radians, float scl, float mag) {
        return cos(radians / scl) * mag;
    }

    // 角度计算
    public static float angle(float x, float y) {
        float result = atan2(x, y) * radDeg;
        if (result < 0) result += 360f;
        return result;
    }

    public static float angleExact(float x, float y) {
        float result = (float) Math.atan2(y, x) * radDeg;
        if (result < 0) result += 360f;
        return result;
    }

    public static float wrapAngleAroundZero(float a) {
        if (a >= 0) {
            float rotation = a % PI2;
            if (rotation > PI) rotation -= PI2;
            return rotation;
        } else {
            float rotation = -a % PI2;
            if (rotation > PI) rotation -= PI2;
            return -rotation;
        }
    }

    private static float atn(final double i) {
        final double n = Math.abs(i);
        final double c = (n - 1.0) / (n + 1.0);
        final double c2 = c * c, c3 = c * c2, c5 = c3 * c2, c7 = c5 * c2, c9 = c7 * c2, c11 = c9 * c2;
        return (float) Math.copySign((Math.PI * 0.25)
                + (0.99997726 * c - 0.33262347 * c3 + 0.19354346 * c5 - 0.11643287 * c7 + 0.05265332 * c9 - 0.0117212 * c11), i);
    }

    public static float atan2(float x, final float y) {
        if (x == 0 && y == 0) return 0;
        
        float n = y / x;
        if (Float.isNaN(n)) {
            n = (y == x ? 1f : -1f);
        } else if (Float.isInfinite(n)) {
            x = 0f;
        }

        if (x > 0) {
            return atn(n);
        } else if (x < 0) {
            return y >= 0 ? atn(n) + PI : atn(n) - PI;
        } else if (y > 0) {
            return halfPi;
        } else if (y < 0) {
            return -halfPi;
        } else {
            return 0;
        }
    }

    // 数值操作
    public static int digits(int n) {
        return n < 100000 ? n < 100 ? n < 10 ? 1 : 2 : n < 1000 ? 3 : n < 10000 ? 4 : 5 : 
               n < 10000000 ? n < 1000000 ? 6 : 7 : n < 100000000 ? 8 : n < 1000000000 ? 9 : 10;
    }

    public static int digits(long n) {
        return n == 0 ? 1 : (int) (Math.log10(n) + 1);
    }

    public static float sqrt(float x) {
        return (float) Math.sqrt(x);
    }

    public static float sqr(float x) {
        return x * x;
    }

    public static float map(float value, float froma, float toa, float fromb, float tob) {
        return fromb + (value - froma) * (tob - fromb) / (toa - froma);
    }

    public static float map(float value, float from, float to) {
        return map(value, 0, 1, from, to);
    }

    public static int sign(float f) {
        return (f < 0 ? -1 : 1);
    }

    public static int sign(boolean b) {
        return b ? 1 : -1;
    }

    public static int num(boolean b) {
        return b ? 1 : 0;
    }

    public static float pow(float a, float b) {
        return (float) Math.pow(a, b);
    }

    public static int pow(int a, int b) {
        return (int) Math.ceil(Math.pow(a, b));
    }

    // 随机数
    public static float range(float range) {
        return random(-range, range);
    }

    public static int range(int range) {
        return random(-range, range);
    }

    public static float range(float min, float max) {
        if (chance(0.5)) {
            return random(min, max);
        } else {
            return -random(min, max);
        }
    }

    public static boolean chanceDelta(double d) {
        return rand.nextFloat() < d * 1/60f; // 简化处理，假设60fps
    }

    public static boolean chance(double d) {
        return d >= 1f || rand.nextFloat() < d;
    }

    public static int random(int range) {
        return rand.nextInt(range + 1);
    }

    public static int random(int start, int end) {
        return start + rand.nextInt(end - start + 1);
    }

    public static long random(long range) {
        return (long) (rand.nextDouble() * range);
    }

    public static long random(long start, long end) {
        return start + (long) (rand.nextDouble() * (end - start));
    }

    public static boolean randomBoolean() {
        return rand.nextBoolean();
    }

    public static boolean randomBoolean(float chance) {
        return random() < chance;
    }

    public static float random() {
        return rand.nextFloat();
    }

    public static float random(float range) {
        return rand.nextFloat() * range;
    }

    public static float random(float start, float end) {
        return start + rand.nextFloat() * (end - start);
    }

    public static int randomSign() {
        return 1 | (rand.nextInt() >> 31);
    }

    public static int randomSeed(long seed, int min, int max) {
        seedr.setSeed(seed);
        if (isPowerOfTwo(max)) {
            seedr.nextInt();
        }
        return seedr.nextInt(max - min + 1) + min;
    }

    public static float randomSeed(long seed, float min, float max) {
        seedr.setSeed(seed);
        return (min + seedr.nextFloat() * (max - min));
    }

    public static float randomSeed(long seed) {
        seedr.setSeed(seed * 99999);
        return seedr.nextFloat();
    }

    public static float randomSeed(long seed, float max) {
        seedr.setSeed(seed * 99999);
        return seedr.nextFloat() * max;
    }

    public static float randomSeedRange(long seed, float range) {
        seedr.setSeed(seed * 99999);
        return range * (seedr.nextFloat() - 0.5f) * 2f;
    }

    public static float randomTriangular() {
        return rand.nextFloat() - rand.nextFloat();
    }

    public static float randomTriangular(float max) {
        return (rand.nextFloat() - rand.nextFloat()) * max;
    }

    public static float randomTriangular(float min, float max) {
        return randomTriangular(min, max, (min + max) * 0.5f);
    }

    public static float randomTriangular(float min, float max, float mode) {
        float u = rand.nextFloat();
        float d = max - min;
        if (u <= (mode - min) / d) return min + (float) Math.sqrt(u * d * (mode - min));
        return max - (float) Math.sqrt((1 - u) * d * (max - mode));
    }

    // 位操作
    public static int nextPowerOfTwo(int value) {
        if (value == 0) return 1;
        value--;
        value |= value >> 1;
        value |= value >> 2;
        value |= value >> 4;
        value |= value >> 8;
        value |= value >> 16;
        return value + 1;
    }

    public static boolean isPowerOfTwo(int value) {
        return value != 0 && (value & value - 1) == 0;
    }

    // 限制函数
    public static int clamp(int value, int min, int max) {
        return Math.max(Math.min(value, max), min);
    }

    public static long clamp(long value, long min, long max) {
        return Math.max(Math.min(value, max), min);
    }

    public static float clamp(float value, float min, float max) {
        return Math.max(Math.min(value, max), min);
    }

    public static float clamp(float value) {
        return Math.max(Math.min(value, 1f), 0f);
    }

    public static double clamp(double value, double min, double max) {
        return Math.max(Math.min(value, max), min);
    }

    public static float maxZero(float val) {
        return Math.max(val, 0);
    }

    // 插值和趋近
    public static float approach(float from, float to, float speed) {
        return from + clamp(to - from, -speed, speed);
    }

    public static float approachDelta(float from, float to, float speed) {
        return approach(from, to, 1/60f * speed); // 简化处理
    }

    public static float lerp(float fromValue, float toValue, float progress) {
        return fromValue + (toValue - fromValue) * progress;
    }

    public static float lerpDelta(float fromValue, float toValue, float progress) {
        return lerp(fromValue, toValue, clamp(progress * 1/60f)); // 简化处理
    }

    public static float slerpRad(float fromRadians, float toRadians, float progress) {
        float delta = ((toRadians - fromRadians + PI2 + PI) % PI2) - PI;
        return (fromRadians + delta * progress + PI2) % PI2;
    }

    public static float slerp(float fromDegrees, float toDegrees, float progress) {
        float delta = ((toDegrees - fromDegrees + 360 + 180) % 360) - 180;
        return (fromDegrees + delta * progress + 360) % 360;
    }

    public static float slerpDelta(float fromDegrees, float toDegrees, float progress) {
        return slerp(fromDegrees, toDegrees, clamp(progress * 1/60f)); // 简化处理
    }

    // 取整函数
    public static int floor(float value) {
        return (int) (value + BIG_ENOUGH_FLOOR) - BIG_ENOUGH_INT;
    }

    public static int floorPositive(float value) {
        return (int) value;
    }

    public static int ceil(float value) {
        return BIG_ENOUGH_INT - (int) (BIG_ENOUGH_FLOOR - value);
    }

    public static int ceilPositive(float value) {
        return (int) (value + CEIL);
    }

    public static int round(float value) {
        return (int) (value + BIG_ENOUGH_ROUND) - BIG_ENOUGH_INT;
    }

    public static int round(int value, int step) {
        return (value / step) * step;
    }

    public static float round(float value, float step) {
        return (int) (value / step) * step;
    }

    public static int round(float value, int step) {
        return (int) (value / step) * step;
    }

    public static int roundPositive(float value) {
        return (int) (value + 0.5f);
    }

    // 比较函数
    public static boolean zero(float value) {
        return Math.abs(value) <= FLOAT_ROUNDING_ERROR;
    }

    public static boolean zero(double value) {
        return Math.abs(value) <= FLOAT_ROUNDING_ERROR;
    }

    public static boolean zero(float value, float tolerance) {
        return Math.abs(value) <= tolerance;
    }

    public static boolean equal(float a, float b) {
        return Math.abs(a - b) <= FLOAT_ROUNDING_ERROR;
    }

    public static boolean equal(float a, float b, float tolerance) {
        return Math.abs(a - b) <= tolerance;
    }

    // 对数函数
    public static float log(float a, float value) {
        return (float) (Math.log(value) / Math.log(a));
    }

    public static float log2(float value) {
        return log(2, value);
    }

    public static int log2(int value) {
        return value == 0 ? 0 : 31 - Integer.numberOfLeadingZeros(value);
    }

    // 模运算
    public static float mod(float f, float n) {
        return ((f % n) + n) % n;
    }

    public static int mod(int x, int n) {
        return ((x % n) + n) % n;
    }

    // 采样和曲线
    public static float sample(float[] values, float time) {
        time = clamp(time);
        float pos = time * (values.length - 1);
        int cur = Math.min((int) (time * (values.length - 1)), values.length - 1);
        int next = Math.min(cur + 1, values.length - 1);
        float mod = (pos - cur);
        return lerp(values[cur], values[next], mod);
    }

    public static float slope(float fin) {
        return 1f - Math.abs(fin - 0.5f) * 2f;
    }

    public static float curve(float f, float offset) {
        if (f < offset) {
            return 0f;
        } else {
            return (f - offset) / (1f - offset);
        }
    }

    public static float curve(float f, float from, float to) {
        if (f < from) {
            return 0f;
        } else if (f > to) {
            return 1f;
        } else {
            return (f - from) / (to - from);
        }
    }

    public static float curveMargin(float f, float margin) {
        return curveMargin(f, margin, margin);
    }

    public static float curveMargin(float f, float marginLeft, float marginRight) {
        if (f < marginLeft) return f / marginLeft * 0.5f;
        if (f > 1f - marginRight) return (f - 1f + marginRight) / marginRight * 0.5f + 0.5f;
        return 0.5f;
    }

    // 几何函数
    public static float len(float x, float y) {
        return (float) Math.sqrt(x * x + y * y);
    }

    public static float len2(float x, float y) {
        return x * x + y * y;
    }

    public static float dot(float x1, float y1, float x2, float y2) {
        return x1 * x2 + y1 * y2;
    }

    public static float dst(float x1, float y1) {
        return (float) Math.sqrt(x1 * x1 + y1 * y1);
    }

    public static float dst2(float x1, float y1) {
        return (x1 * x1 + y1 * y1);
    }

    public static float dst(float x1, float y1, float x2, float y2) {
        final float xd = x2 - x1;
        final float yd = y2 - y1;
        return (float) Math.sqrt(xd * xd + yd * yd);
    }

    public static float dst2(float x1, float y1, float x2, float y2) {
        final float xd = x2 - x1;
        final float yd = y2 - y1;
        return xd * xd + yd * yd;
    }

    public static float dstm(float x1, float y1, float x2, float y2) {
        return Math.abs(x1 - x2) + Math.abs(y1 - y2);
    }

    public static Vector2 arrive(float x, float y, float destX, float destY, Vector2 curVel, float radius, float tolerance, float speed, float accel) {
        Vector2 toTarget = v1.set(destX, destY).sub(x, y);
        float distance = toTarget.len();

        if (distance <= tolerance) return v3.setZero();
        float targetSpeed = speed;
        if (distance <= radius) targetSpeed *= distance / radius;

        return toTarget.sub(curVel.x / accel, curVel.y / accel).limit(targetSpeed);
    }

    public static boolean within(float x1, float y1, float x2, float y2, float dst) {
        return dst2(x1, y1, x2, y2) < dst * dst;
    }

    public static boolean within(float x1, float y1, float dst) {
        return (x1 * x1 + y1 * y1) < dst * dst;
    }
}