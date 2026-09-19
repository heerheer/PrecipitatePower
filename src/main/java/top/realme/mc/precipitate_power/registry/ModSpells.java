package top.realme.mc.precipitate_power.registry;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import top.realme.mc.precipitate_power.PrecipitatePower;
import top.realme.mc.precipitate_power.spell.GiantSockSpell;
import top.realme.mc.precipitate_power.spell.BloodiPowerSpell;
import top.realme.mc.precipitate_power.spell.SockBarrageSpell;
import top.realme.mc.precipitate_power.spell.SockOfferingSpell;
import top.realme.mc.precipitate_power.spell.SockWardSpell;

public final class ModSpells {
    public static final DeferredRegister<AbstractSpell> REGISTER =
            DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, PrecipitatePower.MODID);

    public static final DeferredHolder<AbstractSpell, GiantSockSpell> GIANT_SOCK =
            REGISTER.register("giant_sock", GiantSockSpell::new);
    public static final DeferredHolder<AbstractSpell, SockBarrageSpell> SOCK_BARRAGE =
            REGISTER.register("sock_barrage", SockBarrageSpell::new);
    public static final DeferredHolder<AbstractSpell, SockWardSpell> SOCK_WARD =
            REGISTER.register("sock_ward", SockWardSpell::new);
    public static final DeferredHolder<AbstractSpell, BloodiPowerSpell> BLOODI_POWER =
            REGISTER.register("bloodi_power", BloodiPowerSpell::new);
    public static final DeferredHolder<AbstractSpell, SockOfferingSpell> SOCK_OFFERING =
            REGISTER.register("sock_offering", SockOfferingSpell::new);

    private ModSpells() {
    }
}
