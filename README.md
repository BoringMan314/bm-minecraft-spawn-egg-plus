# [B.M] Minecraft 生物蛋 PLUS

[![Paper](https://img.shields.io/badge/Paper-26.3-2D2D2D)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![GitHub](https://img.shields.io/badge/GitHub-bm--minecraft--spawn--egg--plus-181717?logo=github)](https://github.com/BoringMan314/bm-minecraft-spawn-egg-plus)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

適用於 **Minecraft Paper 26.3** 的插件：工作檯用九個雞蛋合成捕捉蛋。空蛋可堆疊 64 個，右鍵捕捉生物；捉到之後帶有附魔光效，名稱改為生物名或命名牌名稱，再右鍵放出。

*适用于 **Minecraft Paper 26.3** 的插件：工作台用九个鸡蛋合成捕捉蛋。空蛋可堆叠 64 个，右键捕捉生物；捉到之后带有附魔光效，名称改为生物名或命名牌名称，再右键放出。*<br>
*Minecraft Paper 26.3 向け：作業台で卵9個から捕獲卵を作り、右クリックで生物を捕獲・放出します。*<br>
*A **Minecraft Paper 26.3** plugin that crafts a capture egg from nine eggs, then captures and releases mobs.*

> **說明**：僅支援 Paper 26.3 與 Java 25。捕捉蛋本體是雞蛋，外觀使用快樂幽靈生怪蛋。

---

## 目錄

- [功能](#功能)
- [系統需求](#系統需求)
- [安裝方式](#安裝方式)
- [指令與權限](#指令與權限)
- [設定檔](#設定檔)
- [本機建置](#本機建置)
- [專案結構](#專案結構)
- [版本與多語系](#版本與多語系)
- [資料與隱私說明](#資料與隱私說明)
- [授權](#授權)
- [問題與建議](#問題與建議)

---

## 功能

- **工作檯**：九個雞蛋排成 3×3，合成一個捕捉蛋。名稱為「捕捉蛋」，外觀是快樂幽靈生怪蛋，沒有生物時可堆疊 64 個。
- **捕捉**：手持空的捕捉蛋，右鍵可生成的生物。該生物會被收進蛋裡。蛋變成不可堆疊，並出現附魔光效；有命名牌時名稱用命名牌，否則用生物名稱。
- **放出**：手持已捕捉的蛋，右鍵方塊或空氣，把原來的生物放回。蛋恢復成空的捕捉蛋。
- **關閉功能**：`/bm-minecraft-spawn-egg-plus 0` 只停止合成、捕捉與放出。已做好的蛋維持外觀。
- **移除插件**：插件卸載時，已載入的捕捉蛋會變回雞蛋。重新安裝後，依物品上的資料恢復外觀與捕捉內容。

玩家、盔甲座與無法生成的目標不能捕捉。潛行時不捕捉、不放出，以便照常與方塊或生物互動。

---

## 系統需求

- **Paper 26.3** 伺服器。
- **Java 25**。

---

## 安裝方式

1. 從 [`dist/`](dist/) 選擇所需語系 JAR。
2. 將 JAR 放入 Paper 伺服器的 `plugins/` 資料夾。
3. 啟動伺服器後，設定檔建立於 `plugins/bm-minecraft-spawn-egg-plus/`。

> 請勿同時安裝多個語系 JAR；它們是同一插件的不同預設語言版本。

---

## 指令與權限

| 指令 | 說明 | 權限 |
| --- | --- | --- |
| `/bm-minecraft-spawn-egg-plus 0/1` | 開關功能 | `.admin` |
| `/bm-minecraft-spawn-egg-plus reload` | 重載設定 | `.admin` |
| `/bm-minecraft-spawn-egg-plus info` | 顯示資訊 | `.admin` |
| `/bm-minecraft-spawn-egg-plus status` | 顯示狀態 | `.admin` |

`bm-minecraft-spawn-egg-plus.use` 預設所有玩家可合成、捕捉與放出；`.admin` 預設 OP 可用。

---

## 設定檔

```yml
enabled: true
admin-require-op: true
```

`enabled` 關閉時不能合成、捕捉、放出。管理指令仍可用。

---

## 本機建置

執行 `build.bat`；預設會在結束時暫停。自動化環境使用：

```bat
build.bat --no-pause
```

會在 `dist/` 產生 `zh_TW`、`zh_CN`、`ja_JP`、`en_US` JAR。

---

## 專案結構

```text
src/main/java/bm.minecraft.spawn.egg.plus/
src/main/resources/lang/
src/main/resources/config.yml
```

---

## 版本與多語系

版本為 `26.3_0.0.1`；建置時以 `active-language.yml` 選擇四種語系之一。

空蛋名稱使用語系檔的 `item-name`。捉到的生物若沒有命名牌，名稱使用客戶端的生物翻譯。

---

## 資料與隱私說明

本插件不另外儲存玩家資料。捕捉內容寫在該雞蛋的 PDC 上，僅供本機伺服器捕捉與放出使用。

---

## 授權

本專案以 [MIT License](LICENSE) 授權。

---

## 問題與建議

歡迎透過 [GitHub Issues](https://github.com/BoringMan314/bm-minecraft-spawn-egg-plus/issues) 回報錯誤或提出改善建議。回報時請一併提供 Paper 版本、Java 版本、設定檔與完整錯誤日誌。
