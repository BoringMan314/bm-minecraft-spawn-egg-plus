# [B.M] Minecraft 生物蛋 PLUS

[![Paper](https://img.shields.io/badge/Paper-26.3-2D2D2D)](https://papermc.io/)
[![Java](https://img.shields.io/badge/Java-25-ED8B00?logo=openjdk&logoColor=white)](https://openjdk.org/)
[![GitHub](https://img.shields.io/badge/GitHub-bm--minecraft--spawn--egg--plus-181717?logo=github)](https://github.com/BoringMan314/bm-minecraft-spawn-egg-plus)
[![GitHub all releases](https://img.shields.io/github/downloads/BoringMan314/bm-minecraft-spawn-egg-plus/total)](https://github.com/BoringMan314/bm-minecraft-spawn-egg-plus/releases)
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
- [本機開發與測試](#本機開發與測試)
- [技術概要](#技術概要)
- [專案結構](#專案結構)
- [版本與多語系](#版本與多語系)
- [資料與隱私說明](#資料與隱私說明)
- [維護者：更新 GitHub 與發行版本](#維護者更新-github-與發行版本)
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

### 從 GitHub Releases 安裝

若 [GitHub Releases](https://github.com/BoringMan314/bm-minecraft-spawn-egg-plus/releases) 已提供 JAR，請選擇所需語系下載；尚無發行檔時，可依下方步驟自行建置。

1. 停止伺服器，將所選 JAR 放入 `plugins/` 資料夾。
2. 啟動 **Paper 26.3** 伺服器，確認控制台顯示插件已啟用。
3. 在 `plugins/bm-minecraft-spawn-egg-plus/` 調整設定；指令與設定項目請見下方說明。

> 同一插件只安裝一份語系 JAR；更新時請移除舊版 JAR。

### 從原始碼建置

1. 點選本頁綠色 **Code** → **Download ZIP** 解壓，或執行 `git clone https://github.com/BoringMan314/bm-minecraft-spawn-egg-plus.git`。
2. 依 [本機開發與測試](#本機開發與測試) 準備 JDK 與 Maven，執行建置。
3. 從本機 `dist/` 選取 `bm-minecraft-spawn-egg-plus_26.3_0.0.1-<語系>.jar`，依上方步驟安裝。

---

## 指令與權限

| 指令 | 說明 | 權限 |
|------|------|------|
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

## 本機開發與測試

**Windows / PowerShell：**

1. 準備 **JDK 25**：放在 `.tools/<JDK 資料夾>/`，或安裝至可由 Windows `JavaSoft\JDK\25` 登錄項目辨識的位置。
2. 準備 **Maven**：放在 `.tools/apache-maven-3.9.11/`，或讓 `mvn.cmd` 可從 `PATH` 執行。首次建置需連線下載依賴。
3. 在專案根目錄執行：

```powershell
.\build.bat --no-pause
```

建置完成後，`dist/` 會產生 `zh_TW`、`zh_CN`、`ja_JP`、`en_US` 四份語系 JAR。直接執行 `build.bat` 會在結束時暫停；`--no-pause` 適合終端與自動化使用。

修改 [`src/main/java/`](src/main/java/) 或 [`src/main/resources/`](src/main/resources/) 後，重新建置並更換測試伺服器的 JAR，重新啟動伺服器，驗證 [功能](#功能) 及 [指令與權限](#指令與權限) 中的操作。`dist/`、`target/` 與 `.tools/` 由 [`.gitignore`](.gitignore) 排除，不會隨原始碼上傳。

---

## 技術概要

- **核心實作**：註冊捕捉蛋合成配方，透過互動事件捕捉及釋放生物，將捕捉內容保存在物品 PDC。
- **指令與權限**：由 [`plugin.yml`](src/main/resources/plugin.yml) 宣告，實際管理限制由指令處理程式與設定共同決定。
- **建置與語系**：使用 [`pom.xml`](pom.xml) 定義依賴，由 [`build.bat`](build.bat) 依語系逐次執行 Maven 建置。

---

## 專案結構

| 路徑 | 說明 |
|------|------|
| [`pom.xml`](pom.xml) | 插件版本、Java 版本、Paper API 依賴與 Maven 建置設定 |
| [`build.bat`](build.bat) | Windows 四語系 JAR 建置腳本 |
| [`src/main/java/bm.minecraft.spawn.egg.plus/`](src/main/java/bm.minecraft.spawn.egg.plus/) | 插件主類別與功能實作 |
| [`src/main/resources/plugin.yml`](src/main/resources/plugin.yml) | 插件資訊、指令與權限宣告 |
| [`src/main/resources/config.yml`](src/main/resources/config.yml) | 功能與管理設定 |
| [`src/main/resources/active-language.yml`](src/main/resources/active-language.yml) | 建置時套用的預設語系 |
| [`src/main/resources/lang/`](src/main/resources/lang/) | 四種語系的訊息 |
| [`.gitignore`](.gitignore) | 本機工具、暫存與建置產物的排除規則 |

---

## 版本與多語系

- **插件版本**：目前為 `26.3_0.0.1`，建置設定見 [`pom.xml`](pom.xml)。
- **目標 API**：Paper `26.3`；實際依賴版本見 `pom.xml` 的 `paper.version`。
- **預設語系**：`zh_TW`，由 Maven 的 `default.language` 設定。
- **內建語系**：`zh_TW`、`zh_CN`、`ja_JP`、`en_US`（路徑為 `src/main/resources/lang/<語系>.yml`）。
- **語系選擇**：建置腳本將不同預設語系分別打包為 JAR；執行時依 `active-language.yml` 載入對應語系。

捕捉蛋中的生物若沒有命名牌，名稱使用客戶端的生物翻譯。

---

## 資料與隱私說明

本插件不另外儲存玩家資料。捕捉內容寫在該雞蛋的 PDC 上，僅供本機伺服器捕捉與放出使用。

---

## 維護者：更新 GitHub 與發行版本

### 更新至 GitHub

在專案根目錄執行：

```powershell
git add README.md
git commit -m "V26.3_0.0.1"
git push origin main
```

### 準備發行檔

1. 確認 [`pom.xml`](pom.xml)、[`build.bat`](build.bat) 與 [`plugin.yml`](src/main/resources/plugin.yml) 中的版本設定一致。
2. 執行 `build.bat --no-pause`，並在測試伺服器驗證功能及語系顯示。
3. 在 [GitHub Releases](https://github.com/BoringMan314/bm-minecraft-spawn-egg-plus/releases) 建立對應版本，附上本機 `dist/` 中的四份語系 JAR 與更新說明。

---

## 授權

本專案以 [MIT License](LICENSE) 授權。

---

## 問題與建議

歡迎透過 [GitHub Issues](https://github.com/BoringMan314/bm-minecraft-spawn-egg-plus/issues) 回報錯誤或提出改善建議。回報時請一併提供 Paper 版本、Java 版本、插件版本、**語系**及重現步驟；若有錯誤，請附上相關設定與錯誤日誌。
