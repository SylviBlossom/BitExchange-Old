package moe.sylvi.bitexchange.compat.kubejs.actions;

import moe.sylvi.bitexchange.bit.info.BitInfo;

@FunctionalInterface
public interface BitInfoAction<I extends BitInfo> {
    I apply(I info);
}
