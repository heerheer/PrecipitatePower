
# 果燃蕉

果燃蕉是可以直接放入沉淀发电机的物品，不需要制成果浆或流体。所有宝箱有 9% 概率产出普通果燃蕉；在末地城宝藏、下界要塞和堡垒遗迹宝箱中，其中准确的 0.09% 会替换为色色果燃蕉。安装水果乐事后，也可以在熔炉或篝火中加热 `fruitydelight:banana`，每个香蕉产出 1 个普通果燃蕉；本模组不额外实现香蕉植物。

普通果燃蕉沿用白袜的沉淀升级和基础发电公式。发电时每 5 秒积累 1 点热量，达到 5 点热量后爆发并额外产生相当于发电机容量 25% 的 FE。每次爆发都会累计爆发次数；第 N 次爆发后，果燃蕉有 `N%` 概率损坏并消失。

普通果燃蕉也可以食用，提供 1 点饥饿值，饱和度系数为 1.0。食用后有 25% 概率获得持续 5 分钟的「肠胃脆弱」。在效果持续期间，3 秒内连续按下 3 次 Shift，会在玩家脚下的方块上生成一个「蕉便便」并立即清除效果。蕉便便使用类似草的十字模型，没有碰撞，可以直接挖取并掉落蕉便便方块；目前仅作为装饰品。

色色果燃蕉也会积累热量和爆发，但每次爆发的损坏概率固定为 0.1%，不会随爆发次数增加。色色果燃蕉每次真正放入发电机输入槽时，都会立即填满发电机的内部电量。

色色果燃蕉还有两种不可逆用途：

- 直接食用需要 64 tick，永久增加 1 点最大生命值。每名玩家最多通过它获得 10 点最大生命值，达到上限后无法继续食用。
- 安装 Farmer's Delight 后，可以在砧板上使用小刀将 1 个色色果燃蕉切成 4 个色色小蕉。每个色色小蕉提供抗性提升 V（30 秒）、生命恢复 V（30 秒）、伤害吸收 V（2 分钟）、力量 III（1 分钟）和防火（5 分钟）。

Installation information
=======

This template repository can be directly cloned to get you started with a new
mod. Simply create a new repository cloned from this one, by following the
instructions provided by [GitHub](https://docs.github.com/en/repositories/creating-and-managing-repositories/creating-a-repository-from-a-template).

Once you have your clone, simply open the repository in the IDE of your choice. The usual recommendation for an IDE is either IntelliJ IDEA or Eclipse.

If at any point you are missing libraries in your IDE, or you've run into problems you can
run `gradlew --refresh-dependencies` to refresh the local cache. `gradlew clean` to reset everything 
{this does not affect your code} and then start the process again.

Mapping Names:
============
By default, the MDK is configured to use the official mapping names from Mojang for methods and fields 
in the Minecraft codebase. These names are covered by a specific license. All modders should be aware of this
license. For the latest license text, refer to the mapping file itself, or the reference copy here:
https://github.com/NeoForged/NeoForm/blob/main/Mojang.md

Additional Resources: 
==========
Community Documentation: https://docs.neoforged.net/  
NeoForged Discord: https://discord.neoforged.net/
