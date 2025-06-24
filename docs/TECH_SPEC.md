# 技術仕様書

## 開発環境

### 基本設定
- **言語**: Kotlin 100%
- **最小SDK**: API 24 (Android 7.0) - 95%+ カバレッジ
- **ターゲットSDK**: API 34 (Android 14)
- **IDE**: Android Studio Iguana以降
- **Gradle**: Kotlin DSL使用

### JVM設定
```kotlin
android {
    compileSdk 34
    
    defaultConfig {
        minSdk 24
        targetSdk 34
        versionCode 1
        versionName "1.0.0"
    }
    
    compileOptions {
        sourceCompatibility JavaVersion.VERSION_1_8
        targetCompatibility JavaVersion.VERSION_1_8
    }
    
    kotlinOptions {
        jvmTarget = "1.8"
    }
}
```

---

## アーキテクチャ

### 基本構成
**MVVM + Repository Pattern + Clean Architecture**

```
UI Layer (Compose + ViewModel)
    ↕
Domain Layer (Use Cases + Repository Interfaces)
    ↕
Data Layer (Repository Implementation + API + Database)
```

### 状態管理
- **StateFlow** でリアクティブな状態管理
- **Hilt** による依存性注入
- **Navigation Compose** による画面遷移

---

## 依存関係設定

### UI・アーキテクチャ
```kotlin
// Jetpack Compose
implementation("androidx.compose.ui:ui:1.6.7")
implementation("androidx.compose.ui:ui-tooling:1.6.7")
implementation("androidx.compose.material3:material3:1.2.1")
implementation("androidx.activity:activity-compose:1.9.0")
implementation("androidx.navigation:navigation-compose:2.7.7")

// ViewModel + StateFlow
implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.0")
implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.0")

// Hilt (依存性注入)
implementation("com.google.dagger:hilt-android:2.51.1")
kapt("com.google.dagger:hilt-compiler:2.51.1")
implementation("androidx.hilt:hilt-navigation-compose:1.2.0")
```

### データベース・API
```kotlin
// Room Database
implementation("androidx.room:room-runtime:2.6.1")
implementation("androidx.room:room-ktx:2.6.1")
kapt("androidx.room:room-compiler:2.6.1")

// ネットワーク
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("com.squareup.okhttp3:logging-interceptor:4.12.0")

// 画像読み込み
implementation("io.coil-kt:coil-compose:2.6.0")
```

### カメラ・バーコードスキャン
```kotlin
// ML Kit バーコードスキャン
implementation("com.google.mlkit:barcode-scanning:17.2.0")

// CameraX
implementation("androidx.camera:camera-camera2:1.3.3")
implementation("androidx.camera:camera-lifecycle:1.3.3")
implementation("androidx.camera:camera-view:1.3.3")
```

### ユーティリティ
```kotlin
// 日付処理
implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.6.0")

// JSON処理
implementation("com.google.code.gson:gson:2.10.1")
```

### デバッグ・開発支援
```kotlin
// Compose デバッグ
debugImplementation("androidx.compose.ui:ui-test-manifest:1.6.7")
debugImplementation("androidx.compose.ui:ui-tooling:1.6.7")
```

---

## プロジェクト構造

```
app/
├── src/main/kotlin/com/readingtracker/
│   ├── di/                           # 依存性注入
│   │   ├── DatabaseModule.kt         # Room DB設定
│   │   ├── NetworkModule.kt          # Retrofit設定
│   │   └── RepositoryModule.kt       # Repository設定
│   │
│   ├── data/                         # データ層
│   │   ├── database/                 # Room関連
│   │   │   ├── entity/               # Entity classes
│   │   │   │   ├── Book.kt
│   │   │   │   ├── ReadingPurpose.kt
│   │   │   │   ├── Memo.kt
│   │   │   │   └── ActionItem.kt
│   │   │   ├── dao/                  # DAO interfaces
│   │   │   │   ├── BookDao.kt
│   │   │   │   ├── ReadingPurposeDao.kt
│   │   │   │   ├── MemoDao.kt
│   │   │   │   └── ActionItemDao.kt
│   │   │   └── AppDatabase.kt        # Database設定
│   │   │
│   │   ├── network/                  # API関連
│   │   │   ├── api/                  # API Service interfaces
│   │   │   │   ├── GoogleBooksApiService.kt
│   │   │   │   └── OpenBdApiService.kt
│   │   │   ├── model/                # API Response models
│   │   │   │   ├── GoogleBooksResponse.kt
│   │   │   │   └── OpenBdResponse.kt
│   │   │   └── ApiRateLimiter.kt     # Rate Limiting
│   │   │
│   │   └── repository/               # Repository実装
│   │       └── BookRepositoryImpl.kt
│   │
│   ├── domain/                       # ドメイン層
│   │   ├── model/                    # Domain models
│   │   │   ├── BookSearchResult.kt
│   │   │   └── ValidationResult.kt
│   │   └── repository/               # Repository interfaces
│   │       └── BookRepository.kt
│   │
│   ├── ui/                           # UI層
│   │   ├── home/                     # ホーム画面
│   │   │   ├── HomeScreen.kt
│   │   │   ├── HomeViewModel.kt
│   │   │   └── HomeUiState.kt
│   │   │
│   │   ├── registration/             # 書籍登録
│   │   │   ├── BookRegistrationScreen.kt
│   │   │   ├── BookRegistrationViewModel.kt
│   │   │   ├── BookRegistrationUiState.kt
│   │   │   └── BarcodeScanner.kt
│   │   │
│   │   ├── detail/                   # 書籍詳細
│   │   │   ├── BookDetailScreen.kt
│   │   │   ├── BookDetailViewModel.kt
│   │   │   ├── BookDetailUiState.kt
│   │   │   ├── MemoAddScreen.kt
│   │   │   └── ActionItemCreateScreen.kt
│   │   │
│   │   ├── statistics/               # 統計画面
│   │   │   ├── StatisticsScreen.kt
│   │   │   ├── StatisticsViewModel.kt
│   │   │   └── StatisticsUiState.kt
│   │   │
│   │   ├── common/                   # 共通UI部品
│   │   │   ├── components/           # 再利用可能なCompose UI
│   │   │   │   ├── BookCard.kt
│   │   │   │   ├── ProgressIndicator.kt
│   │   │   │   └── ErrorMessage.kt
│   │   │   └── theme/                # Material3 テーマ
│   │   │       ├── Color.kt
│   │   │       ├── Theme.kt
│   │   │       └── Type.kt
│   │   │
│   │   └── navigation/               # ナビゲーション
│   │       ├── AppNavigation.kt
│   │       └── Screen.kt
│   │
│   ├── util/                         # ユーティリティ
│   │   ├── DateUtils.kt              # 日付処理
│   │   ├── ValidationUtils.kt        # バリデーション
│   │   └── Constants.kt              # 定数
│   │
│   └── MainActivity.kt               # エントリーポイント
│
├── src/main/res/                     # リソース
│   ├── values/
│   │   ├── strings.xml
│   │   └── colors.xml
│   └── drawable/                     # アイコン・画像
│
└── build.gradle.kts                  # ビルド設定
```

---

## セキュリティ設定

### API キー管理
```kotlin
// local.properties
GOOGLE_BOOKS_API_KEY="your_api_key_here"

// build.gradle.kts
android {
    defaultConfig {
        buildConfigField("String", "GOOGLE_BOOKS_API_KEY", "\"${project.findProperty("GOOGLE_BOOKS_API_KEY")}\"")
    }
}

// 使用方法
val apiKey = BuildConfig.GOOGLE_BOOKS_API_KEY
```

### ネットワークセキュリティ
```kotlin
// network_security_config.xml の設定
android:networkSecurityConfig="@xml/network_security_config"
```

---

## パフォーマンス設定

### R8/ProGuard設定
```kotlin
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
}
```

### メモリ最適化
```kotlin
// 画像読み込み最適化（Coil設定）
ImageLoader.Builder(context)
    .memoryCache {
        MemoryCache.Builder(context)
            .maxSizePercent(0.25)
            .build()
    }
    .build()
```

---

## デバッグ・ログ設定

### OkHttp ログ設定
```kotlin
@Provides
@Singleton
fun provideOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .apply {
            if (BuildConfig.DEBUG) {
                addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
            }
        }
        .build()
}
```

### Room デバッグ設定
```kotlin
@Database(
    entities = [Book::class, ReadingPurpose::class, Memo::class, ActionItem::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    companion object {
        fun create(context: Context): AppDatabase {
            return Room.databaseBuilder(
                context,
                AppDatabase::class.java,
                "reading_tracker_database"
            ).apply {
                if (BuildConfig.DEBUG) {
                    fallbackToDestructiveMigration()
                }
            }.build()
        }
    }
}
```

---

## テスト設定

### 最小限のテスト設定
```kotlin
// テスト用依存関係（必要最小限）
testImplementation("junit:junit:4.13.2")
androidTestImplementation("androidx.test.ext:junit:1.1.5")
androidTestImplementation("androidx.compose.ui:ui-test-junit4:1.6.7")
```

---

## ビルド時間最適化

### Gradle設定
```kotlin
// gradle.properties
org.gradle.jvmargs=-Xmx2048m -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.caching=true
kotlin.code.style=official
android.useAndroidX=true
android.enableJetifier=true
```

---

## 将来の拡張対応

### クラウドDB移行準備
- UUID使用によるプライマリキー設定
- タイムスタンプによる同期対応
- Repository パターンによる実装切り替え可能性

### マルチモジュール化準備
現在は単一モジュール、将来的に以下の分割が可能：
- `:app` - UI層
- `:domain` - ビジネスロジック
- `:data` - データ層
- `:core` - 共通機能

---

この技術仕様に基づき、保守性が高く効率的な開発を行ってください。