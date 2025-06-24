# ReadingTracker - 読書管理アプリ

読書の目的・学び・アクションを管理するAndroidアプリケーション

## 機能概要

- 📚 書籍の登録（バーコードスキャン/手動入力）
- 🎯 読書目的の設定と管理
- 📝 メモ・学びの記録
- ✅ アクションアイテムの作成と追跡
- 📊 読書統計の可視化

## 技術スタック

- **言語**: Kotlin 100%
- **UI**: Jetpack Compose
- **アーキテクチャ**: MVVM + Clean Architecture
- **DI**: Hilt
- **データベース**: Room
- **ネットワーク**: Retrofit
- **最小SDK**: API 24 (Android 7.0)

## セットアップ

1. プロジェクトをクローン
```bash
git clone [repository-url]
```

2. Google Books APIキーを設定
`local.properties`に以下を追加:
```
GOOGLE_BOOKS_API_KEY=your_api_key_here
```

3. Android Studioでプロジェクトを開いて実行

## プロジェクト構造

```
app/
├── src/main/kotlin/com/readingtracker/
│   ├── di/                  # 依存性注入
│   ├── data/                # データ層
│   ├── domain/              # ドメイン層
│   ├── ui/                  # UI層
│   └── util/                # ユーティリティ
```

## 開発状況

現在、初期開発フェーズです。