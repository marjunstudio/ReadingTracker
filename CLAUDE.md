# CLAUDE.md

このファイルは、Claude Code (claude.ai/code) がこのリポジトリで作業する際のガイダンスを提供します。

## プロジェクト概要

ReadingTrackerは、日本のビジネスパーソン向けの読書管理Androidアプリです。「本を最後まで読む」ではなく「3つの実践項目を見つける」ことを読書の成功と再定義し、実践的な成果に焦点を当てています。

## セットアップ要件

アプリを実行する前に必須の設定:
1. `local.properties.example` を `local.properties` にコピー
2. Google Books APIキーを追加: `GOOGLE_BOOKS_API_KEY=your_key_here`
   - https://console.cloud.google.com/ でキーを取得
   - コンソールで "Books API" を有効化

## 開発コマンド一覧

```bash
# ビルド
./gradlew build              # フルビルド
./gradlew assembleDebug      # デバッグAPK作成
./gradlew assembleRelease    # リリースAPK作成
./gradlew clean              # ビルド成果物のクリーン

# テスト
./gradlew test               # ユニットテスト実行
./gradlew connectedAndroidTest  # インストゥルメンテッドテスト（デバイス/エミュレータ必須）

# コード品質
./gradlew lint               # Android Lint実行
./gradlew lintDebug          # デバッグビルドのみLint

# 開発
./gradlew installDebug       # 接続デバイスにインストール
./gradlew dependencies       # 依存関係ツリー表示
```

## アーキテクチャ概要

Clean Architecture + MVVMパターンを採用:

### レイヤー構造
- **プレゼンテーション層**: Jetpack Compose UI + StateFlowを使用したViewModel
- **データ層**: Room（ローカル）とRetrofit（リモート）を仲介するRepositoryパターン
- **DI**: Hiltモジュールによるシングルトンインスタンス提供

### 主要なアーキテクチャパターン

#### データフロー
1. **Repository as Single Source of Truth**: `BookSearchRepository`がキャッシュロジックを管理
   - 書籍データは7日間キャッシュ
   - ネットワーク優先、ローカルフォールバック
   - 日本の書籍（ISBN 9784*）はOpenBD API優先、Google Booksをフォールバック

2. **状態管理**: 単方向データフロー
   - ViewModelは単一の`StateFlow<UiState>`を公開
   - UIは`collectAsStateWithLifecycle()`で状態を監視
   - 状態更新は`_uiState.update { it.copy(...) }`

3. **エラーハンドリング**: シールドクラス`BookSearchResult`
   ```kotlin
   sealed class BookSearchResult {
       data class Success(val book: BookInfo)
       data class NetworkError(val message: String)
       data class RateLimitExceeded(...)
       data class DailyLimitExceeded(...)
       data class ApiError(val message: String)
   }
   ```

#### API統合
- **デュアルAPI戦略**: OpenBD（日本語書籍）+ Google Books（フォールバック）
- **レート制限**: カスタム`ApiRateLimiter`で2秒間隔を強制
- **日次制限**: `DailyRequestTracker`でAPI使用量を監視
- **ネットワークモジュール**: `@Named`修飾子で複数のRetrofitインスタンスを区別

#### データベース設計
- **エンティティの関連**: 外部キーで参照整合性を維持
- **型コンバーター**: enum（`BookStatus`、`PurposeCategory`）と日付を処理
- **リアクティブクエリ**: DAOは`Flow<T>`を返してリアルタイム更新

## 重要な実装詳細

### ナビゲーション
- Compose Navigationを使用したシングルアクティビティ
- ルート経由での引数渡し: `/book/{bookId}`、`/memo/add/{bookId}`
- 専用のシールドクラスによるボトムナビゲーション

### 書籍登録フロー
1. 方法選択：バーコードスキャンまたは手動検索
2. APIから取得した書籍詳細を確認
3. 読書目的を設定（1〜3つ必須）
4. ローカルデータベースに書籍を保存

### ビジネスルール
- **読書目的**: 書籍ごとに1〜3つ（カテゴリ: 💼仕事、🧠考え方、📈スキルアップ、🎯行動、❓興味）
- **メモ**: 書籍ごとに最大10個
- **実践項目**: 書籍ごとに最大3個（「読了」の定義）
- **読書完了**: 3つの実践項目が作成された時点で達成

### ViewModelパターン
```kotlin
@HiltViewModel
class ExampleViewModel @Inject constructor(
    private val repository: Repository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ExampleUiState())
    val uiState = _uiState.asStateFlow()
    
    fun someAction() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // 非同期処理
            _uiState.update { it.copy(isLoading = false, data = result) }
        }
    }
}
```

### モジュール依存関係
- **DatabaseModule**: AppDatabaseと全てのDAOを提供
- **NetworkModule**: 適切な設定でAPIサービスを提供
- 両モジュールはアプリ全体のインスタンスとして`@Singleton`スコープを使用

## 開発上の注意点

- 日本語ローカライゼーションが主要（エラーメッセージ、UIテキスト）
- バーコードスキャンはCameraXでML Kitを使用
- 画像読み込みはCoilを使用
- ネットワークログはデバッグビルドのみ有効
- Edge-to-edgeディスプレイサポート実装済み