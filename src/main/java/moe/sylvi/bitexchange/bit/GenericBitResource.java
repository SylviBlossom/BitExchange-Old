package moe.sylvi.bitexchange.bit;

import moe.sylvi.bitexchange.bit.registry.BitRegistry;

public record GenericBitResource(BitRegistry registry, Object resource, long amount) {}