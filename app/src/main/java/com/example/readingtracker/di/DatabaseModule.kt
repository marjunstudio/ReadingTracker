package com.example.readingtracker.di

import android.content.Context
import androidx.room.Room
import com.example.readingtracker.data.database.AppDatabase
import com.example.readingtracker.data.database.dao.*
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    
    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            AppDatabase.DATABASE_NAME
        )
            .fallbackToDestructiveMigration()
            .build()
    }
    
    @Provides
    @Singleton
    fun provideBookDao(database: AppDatabase): BookDao {
        return database.bookDao()
    }
    
    @Provides
    @Singleton
    fun provideReadingPurposeDao(database: AppDatabase): ReadingPurposeDao {
        return database.readingPurposeDao()
    }
    
    @Provides
    @Singleton
    fun provideMemoDao(database: AppDatabase): MemoDao {
        return database.memoDao()
    }
    
    @Provides
    @Singleton
    fun provideActionItemDao(database: AppDatabase): ActionItemDao {
        return database.actionItemDao()
    }
    
    @Provides
    @Singleton
    fun provideBookCacheDao(database: AppDatabase): BookCacheDao {
        return database.bookCacheDao()
    }
}