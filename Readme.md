# Badget

Badget是frida的持久化工具，通过注入frida-gadget，让目标app加载该so文件，进而实现frida的hook功能。

- gadget版本: 16.5.6 (包含strongR)
- 参考项目  [xcubebase](https://github.com/svengong/xcubebase)
- 参考博客地址: [Frida持久化方案(Xcube)之方案二——基于xposed](https://bbs.kanxue.com/thread-266784.htm)

<div>
<img src="https://github.com/zcfrank1st/badget/blob/main/1.png" width="300">&nbsp;&nbsp;
<img src="https://github.com/zcfrank1st/badget/blob/main/2.png" width="300">
<img src="https://github.com/zcfrank1st/badget/blob/main/3.png" width="300">
<img src="https://github.com/zcfrank1st/badget/blob/main/4.png" width="300">
</div>


```
免责声明：
本项目仅限用于逆向安全领域爱好者学习参考和研究目的，不得用于商业或者非法用途，否则，一切后果请用户自负。
```


# 目录结构
```tree

├── /data/local/tmp/#tree
├── badget
│   ├── arm64-v8a
│   │   └── libfrida_gadget.so
│   ├── arm64-v7a
│   │   └── libfrida_gadget.so
│   ├── com.aaa.bbb
│   │   └── hook.js
│   │
│   └── badget.json
└
```


# 已实现
- 实现界面化配置
- 使用Material Design主题
- gadget库随机命名
- 可选交互类型
  1. Listen
  2. Connect
  3. Script
  4. ScriptDirectory
- 多gadget版本支持，可离线添加


# 未实现
- frida-gadget版本在线下载（需服务端或依赖github，待定）
- ~~监听脚本内容变化，并更新到/data/local/tmp/badget/packageName/hook.js~~
- 脚本仓库(脚本市场)
- ~~脚本日志悬浮窗~~
- 不依赖xposed打包，提供多种打包方式


# V1.3
1.gadget多版本支持

# V1.4
1.logcat 悬浮日志框，可debug查询

## 参考&致谢
- [svengong/xcubebase](https://github.com/svengong/xcubebase)
- [Dr-TSNG/Hide-My-Applist](https://github.com/Dr-TSNG/Hide-My-Applist)
- [SeeFlowerX](https://github.com/SeeFlowerX)
- [poorld/badget](https://github.com/poorld/badget)


