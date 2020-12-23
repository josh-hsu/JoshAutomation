# JoshAutomation
此軟體必須要使用您自己的 Android ROM，或是您可以將 APK 簽屬 Platform key。

## 前置工作
1. 到 [JoshAutomationService](https://github.com/josh-hsu/JoshAutomationService/tree/master/release) 下載最新的 APK 並簽屬 Platform key 後安裝到您的 Android 手機
2. 安裝 [JoshAutomation](https://github.com/josh-hsu/JoshAutomation/tree/master/release) APK 至您的手機

## 設定 JoshAutomation (大小寫注意)
<< 2.0 版本之後此步驟不須設定 >>
1. 打開 JoshAutomation，您必須先同意使用外部儲存空間，按下 打開設定 > 腳本與系統設定
2. 元件名稱請填: com.mumu.joshautomationservice
3. 服務名稱請填: .CommandService      (注意前面有的點 . 不要忽略了)
4. 交易密碼請填: 1
5. 請將啟用 binder 控制打開

## 開始使用
1. 回到首頁按下 啟動服務
2. 同意使用繪圖權限 (只需一次) 再按一次 啟動服務
3. 接下來就可以 Play 按鈕啟動腳本，腳本選擇請到設定頁面選擇
