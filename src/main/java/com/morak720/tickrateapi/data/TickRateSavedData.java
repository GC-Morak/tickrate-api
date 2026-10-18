package com.morak720.tickrateapi.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;
import java.util.ArrayList;
import java.util.List;

public class TickRateSavedData extends SavedData {
    private final List<TickRateArea> areas = new ArrayList<>();

    public TickRateSavedData() {}

    public void addArea(TickRateArea area) {
        this.areas.add(area);
        this.setDirty();
    }

    public void removeArea(TickRateArea area) {
        this.areas.remove(area);
        this.setDirty();
    }

    public void clearAreas() {
        this.areas.clear();
        this.setDirty();
    }

    public List<TickRateArea> getAreas() {
        return areas;
    }

    @Override
    public CompoundTag save(CompoundTag compoundTag, HolderLookup.Provider provider) {
        ListTag list = new ListTag();
        for (TickRateArea area : areas) {
            list.add(area.save());
        }
        compoundTag.put("Areas", list);
        return compoundTag;
    }

    public static TickRateSavedData load(CompoundTag compoundTag, HolderLookup.Provider provider) {
        TickRateSavedData data = new TickRateSavedData();
        if (compoundTag.contains("Areas", Tag.TAG_LIST)) {
            ListTag list = compoundTag.getList("Areas", Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                data.areas.add(TickRateArea.load(list.getCompound(i)));
            }
        }
        return data;
    }

    public static SavedData.Factory<TickRateSavedData> factory() {
        return new SavedData.Factory<>(
            TickRateSavedData::new,
            TickRateSavedData::load,
            null
        );
    }

    public static TickRateSavedData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(factory(), "tickrateapi_areas");
    }
}
