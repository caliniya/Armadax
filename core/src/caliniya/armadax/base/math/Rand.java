package caliniya.armadax.base.math;

import java.util.Random;

public class Rand extends Random{
    /** 双精度浮点数的归一化常数 */
    private static final double NORM_DOUBLE = 1.0 / (1L << 53);
    /** 单精度浮点数的归一化常数 */
    private static final double NORM_FLOAT = 1.0 / (1L << 24);

    /** 伪随机数生成器内部状态的第一部分 */
    public long seed0;
    /** 伪随机数生成器内部状态的第二部分 */
    public long seed1;

    /**
     * 创建一个新的随机数生成器。此构造方法将随机数生成器的种子设置为一个很可能与其他调用不同的值。
     * <p>
     * 此实现创建一个 {@link Random} 实例来生成初始种子。
     */
    public Rand(){
        setSeed(new Random().nextLong());
    }

    /**
     * 使用单个 {@code long} 种子创建一个新的随机数生成器。
     * @param seed 初始种子
     */
    public Rand(long seed){
        setSeed(seed);
    }

    /**
     * 使用两个 {@code long} 种子创建一个新的随机数生成器。
     * @param seed0 初始种子的第一部分
     * @param seed1 初始种子的第二部分
     */
    public Rand(long seed0, long seed1){
        setState(seed0, seed1);
    }

    /**
     * MurmurHash3 哈希函数，用于种子混合
     * @param x 输入值
     * @return 哈希结果
     */
    private static long murmurHash3(long x){
        x ^= x >>> 33;
        x *= 0xff51afd7ed558ccdL;
        x ^= x >>> 33;
        x *= 0xc4ceb9fe1a85ec53L;
        x ^= x >>> 33;

        return x;
    }

    /**
     * 返回此随机数生成器序列中下一个伪随机的、均匀分布的 {@code long} 值。
     * <p>
     * 子类应重写此方法，因为所有其他方法都使用它。
     */
    @Override
    public long nextLong(){
        long s1 = this.seed0;
        final long s0 = this.seed1;
        this.seed0 = s0;
        s1 ^= s1 << 23;
        return (this.seed1 = (s1 ^ s0 ^ (s1 >>> 17) ^ (s0 >>> 26))) + s0;
    }

    /** 此受保护方法是 final 的，因为与超类不同，它不再被其他方法使用。 */
    @Override
    protected final int next(int bits){
        return (int)(nextLong() & ((1L << bits) - 1));
    }

    /**
     * 返回此随机数生成器序列中下一个伪随机的、均匀分布的 {@code int} 值。
     * <p>
     * 此实现在内部使用 {@link #nextLong()}。
     */
    @Override
    public int nextInt(){
        return (int)nextLong();
    }

    /**
     * 返回此随机数生成器序列中一个伪随机的、均匀分布的 {@code int} 值，介于 0（包含）和指定值（不包含）之间。
     * <p>
     * 此实现在内部使用 {@link #nextLong()}。
     * @param n 要返回的随机数的正数边界
     * @return 下一个伪随机 {@code int} 值，介于 {@code 0}（包含）和 {@code n}（不包含）之间
     */
    @Override
    public int nextInt(final int n){
        return (int)nextLong(n);
    }

    /**
     * 返回此随机数生成器序列中一个伪随机的、均匀分布的 {@code long} 值，介于 0（包含）和指定值（不包含）之间。
     * 生成该值的算法保证结果是均匀分布的，前提是此生成器产生的 64 位值序列是均匀的。
     * <p>
     * 此实现在内部使用 {@link #nextLong()}。
     * @param n 要返回的随机数的正数边界
     * @return 下一个伪随机 {@code long} 值，介于 {@code 0}（包含）和 {@code n}（不包含）之间
     */
    public long nextLong(final long n){
        if(n <= 0) throw new IllegalArgumentException("n 必须是正数");
        for(;;){
            final long bits = nextLong() >>> 1;
            final long value = bits % n;
            if(bits - value + (n - 1) >= 0) return value;
        }
    }

    /**
     * 返回此随机数生成器序列中一个伪随机的、均匀分布的 {@code double} 值，介于 0.0 和 1.0 之间。
     * <p>
     * 此实现在内部使用 {@link #nextLong()}。
     */
    @Override
    public double nextDouble(){
        return (nextLong() >>> 11) * NORM_DOUBLE;
    }

    /**
     * 返回此随机数生成器序列中一个伪随机的、均匀分布的 {@code float} 值，介于 0.0 和 1.0 之间。
     * <p>
     * 此实现在内部使用 {@link #nextLong()}。
     */
    @Override
    public float nextFloat(){
        return (float)((nextLong() >>> 40) * NORM_FLOAT);
    }

    /**
     * 返回此随机数生成器序列中一个伪随机的、均匀分布的 {@code boolean} 值。
     * <p>
     * 此实现在内部使用 {@link #nextLong()}。
     */
    @Override
    public boolean nextBoolean(){
        return (nextLong() & 1) != 0;
    }

    /**
     * 生成随机字节并将其放入用户提供的字节数组中。生成的随机字节数等于字节数组的长度。
     * <p>
     * 此实现在内部使用 {@link #nextLong()}。
     */
    @Override
    public void nextBytes(final byte[] bytes){
        int n;
        int i = bytes.length;
        while(i != 0){
            n = i < 8 ? i : 8; // min(i, 8);
            for(long bits = nextLong(); n-- != 0; bits >>= 8)
                bytes[--i] = (byte)bits;
        }
    }

    /**
     * 根据给定的 {@code long} 值设置此生成器的内部种子。
     * <p>
     * 给定的种子通过哈希函数传递两次。这样，如果用户传递一个较小的值，我们可以避免在状态位数非常少时出现的短暂不规则瞬态。
     * @param seed 此生成器的非零种子（如果为零，生成器将使用 {@link Long#MIN_VALUE} 作为种子）
     */
    @Override
    public void setSeed(final long seed){
        long seed0 = murmurHash3(seed == 0 ? Long.MIN_VALUE : seed);
        setState(seed0, murmurHash3(seed0));
    }

    /**
     * 以给定概率返回 true
     * @param chance 概率值，范围 [0, 1]
     * @return 如果随机数小于给定概率则返回 true
     */
    public boolean chance(double chance){
        return nextDouble() < chance;
    }

    /**
     * 返回 [-amount, amount] 范围内的随机浮点数
     * @param amount 范围大小
     * @return 随机浮点数
     */
    public float range(float amount){
        return nextFloat() * amount * 2 - amount;
    }

    /**
     * 返回 [0, max] 范围内的随机浮点数
     * @param max 最大值
     * @return 随机浮点数
     */
    public float random(float max){
        return nextFloat() * max;
    }

    /**
     * 返回 [0, max] 范围内的随机整数（包含）
     * @param max 最大值
     * @return 随机整数
     */
    public int random(int max){
        return nextInt(max + 1);
    }

    /**
     * 返回 [min, max] 范围内的随机浮点数
     * @param min 最小值
     * @param max 最大值
     * @return 随机浮点数
     */
    public float random(float min, float max){
        return min + (max - min) * nextFloat();
    }

    /**
     * 返回 [-amount, amount] 范围内的随机整数
     * @param amount 范围大小
     * @return 随机整数
     */
    public int range(int amount){
        return nextInt(amount * 2 + 1) - amount;
    }

    /**
     * 返回 [min, max] 范围内的随机整数（包含）
     * @param min 最小值
     * @param max 最大值
     * @return 随机整数
     */
    public int random(int min, int max){
        if(min >= max) return min;
        return min + nextInt(max - min + 1);
    }

    /**
     * 设置此生成器的内部状态
     * @param seed0 内部状态的第一部分
     * @param seed1 内部状态的第二部分
     */
    public void setState(final long seed0, final long seed1){
        this.seed0 = seed0;
        this.seed1 = seed1;
    }

    /**
     * 返回内部种子以允许状态保存
     * @param seed 必须为 0 或 1，指定要返回的 2 个长整数种子中的哪一个
     * @return 可用于 setState 的内部种子
     */
    public long getState(int seed){
        return seed == 0 ? seed0 : seed1;
    }
}