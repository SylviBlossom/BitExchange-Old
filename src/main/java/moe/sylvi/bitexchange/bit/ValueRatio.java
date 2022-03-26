package moe.sylvi.bitexchange.bit;

public record ValueRatio(double value, long ratio) {
    public double get(long amount) {
        return (value / ratio) * amount;
    }
}
