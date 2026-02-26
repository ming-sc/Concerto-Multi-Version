# 为什么？

ServiceLoader 无法正常加载音频库的 ServiceProvider 实现类, 导致无法使用音频库.

这个问题在 McModLauncher/modlauncher
中被提出 ([McModLauncher/modlauncher#100](https://github.com/McModLauncher/modlauncher/issues/100)),
在 McModLauncher/securejarhandler
中被修复 ([McModLauncher/securejarhandler#52](https://github.com/McModLauncher/securejarhandler/pull/52)),
但 [MinecraftForge/SecureModules](https://github.com/MinecraftForge/SecureModules/)
未同步该修复, 导致问题在 forge 依旧存在.

所以这里复制音频库所有支持格式的 FileReader 和 FormatConversionProvider 解决该问题. 
> [!WARNING]
> 复制的 FileReader 和 FormatConversionProvider 文件中有少量修改.